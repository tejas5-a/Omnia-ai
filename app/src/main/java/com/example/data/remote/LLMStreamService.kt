package com.example.data.remote

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object LLMStreamService {

    private const val TAG = "LLMStreamService"

    fun generateStream(
        provider: ProviderConfig,
        modelId: String,
        messages: List<ChatMessage>,
        systemInstruction: String,
        isHighThinking: Boolean = false
    ): Flow<String> = flow {
        val client = ApiClient.okHttpClient
        val startTime = System.currentTimeMillis()
        var chunkCount = 0
        var firstChunkTime = -1L

        val effectiveModelId = modelId.ifBlank { provider.selectedModel }
        Log.d(TAG, "Starting stream request for provider=${provider.name}, type=${provider.type}, model=$effectiveModelId")

        when (provider.type) {
            ProviderType.GEMINI -> {
                val apiKey = provider.apiKey.ifBlank { com.example.BuildConfig.GEMINI_API_KEY }
                if (apiKey.isBlank()) {
                    emit("Error: Gemini API key is missing. Please configure your key in settings.")
                    return@flow
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModelId:streamGenerateContent?alt=sse&key=$apiKey"

                val requestJson = JSONObject()

                if (systemInstruction.isNotBlank()) {
                    val sysObj = JSONObject()
                    val sysParts = JSONArray()
                    sysParts.put(JSONObject().put("text", systemInstruction))
                    sysObj.put("parts", sysParts)
                    requestJson.put("systemInstruction", sysObj)
                }

                if (isHighThinking || effectiveModelId.contains("argon") || effectiveModelId.contains("thinking")) {
                    val genConfig = JSONObject()
                    val thinkingConfig = JSONObject().put("thinkingLevel", "HIGH")
                    genConfig.put("thinkingConfig", thinkingConfig)
                    requestJson.put("generationConfig", genConfig)
                }

                val contentsArray = JSONArray()
                for (msg in messages) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    val contentObj = JSONObject()
                    contentObj.put("role", role)
                    val partsArray = JSONArray()

                    if (msg.content.isNotBlank()) {
                        partsArray.put(JSONObject().put("text", msg.content))
                    }

                    for (att in msg.attachments) {
                        if (!att.base64Data.isNullOrBlank()) {
                            val inlineData = JSONObject()
                            inlineData.put("mimeType", att.mimeType)
                            inlineData.put("data", att.base64Data)
                            partsArray.put(JSONObject().put("inlineData", inlineData))
                        }
                    }

                    if (partsArray.length() > 0) {
                        contentObj.put("parts", partsArray)
                        contentsArray.put(contentObj)
                    }
                }
                requestJson.put("contents", contentsArray)

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                Log.d(TAG, "Gemini HTTP response status: ${response.code}")

                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    emit("Error (${response.code}): $errBody")
                    return@flow
                }

                val reader = response.body?.byteStream()?.bufferedReader() ?: run {
                    emit("Error: Response body was null")
                    return@flow
                }

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val payload = rawLine.removePrefix("data:").trim()
                    if (payload.isEmpty() || payload == "[DONE]") continue

                    try {
                        val json = JSONObject(payload)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val cand = candidates.getJSONObject(0)
                            val content = cand.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    val text = part.optString("text", "")
                                    if (text.isNotEmpty()) {
                                        if (firstChunkTime < 0) firstChunkTime = System.currentTimeMillis() - startTime
                                        chunkCount++
                                        emit(text)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing Gemini chunk: ${e.localizedMessage}")
                    }
                }
            }

            ProviderType.OPENAI, ProviderType.OPENROUTER, ProviderType.POLLINATIONS, ProviderType.CUSTOM -> {
                val baseUrl = provider.baseUrl.ifBlank { provider.type.defaultBaseUrl }.trimEnd('/')
                val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

                val requestJson = JSONObject()
                requestJson.put("model", effectiveModelId)
                requestJson.put("stream", true)

                val messagesArray = JSONArray()

                if (systemInstruction.isNotBlank()) {
                    val sysObj = JSONObject()
                    sysObj.put("role", "system")
                    sysObj.put("content", systemInstruction)
                    messagesArray.put(sysObj)
                }

                for (msg in messages) {
                    val role = when (msg.sender) {
                        MessageSender.USER -> "user"
                        MessageSender.ASSISTANT -> "assistant"
                        MessageSender.AGENT -> "assistant"
                        MessageSender.SYSTEM -> "system"
                    }
                    val msgObj = JSONObject()
                    msgObj.put("role", role)
                    msgObj.put("content", msg.content)
                    messagesArray.put(msgObj)
                }
                requestJson.put("messages", messagesArray)

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val requestBuilder = Request.Builder().url(url).post(body)

                if (provider.apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${provider.apiKey}")
                }
                if (provider.type == ProviderType.OPENROUTER) {
                    requestBuilder.addHeader("HTTP-Referer", "https://github.com/omnia-max")
                    requestBuilder.addHeader("X-Title", "OMNIA MAX")
                }

                val response = client.newCall(requestBuilder.build()).execute()
                Log.d(TAG, "OpenAI/OpenRouter HTTP response status: ${response.code}")

                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    emit("Error (${response.code}): $errBody")
                    return@flow
                }

                val reader = response.body?.byteStream()?.bufferedReader() ?: run {
                    emit("Error: Response body was null")
                    return@flow
                }

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val payload = rawLine.removePrefix("data:").trim()
                    if (payload.isEmpty() || payload == "[DONE]") continue

                    try {
                        val json = JSONObject(payload)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val choice = choices.getJSONObject(0)
                            val delta = choice.optJSONObject("delta")
                            var text = delta?.optString("content", "") ?: ""

                            // Support reasoning / thinking content from OpenRouter / DeepSeek models
                            if (text.isEmpty() && delta != null && delta.has("reasoning_content")) {
                                text = delta.optString("reasoning_content", "")
                            }

                            // Fallback to message object if non-streaming or full message payload
                            if (text.isEmpty()) {
                                val messageObj = choice.optJSONObject("message")
                                if (messageObj != null) {
                                    text = messageObj.optString("content", "")
                                }
                            }

                            if (text.isNotEmpty()) {
                                if (firstChunkTime < 0) firstChunkTime = System.currentTimeMillis() - startTime
                                chunkCount++
                                emit(text)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing delta chunk: ${e.localizedMessage}")
                    }
                }
            }
        }
        Log.d(TAG, "Stream completed. Chunks=$chunkCount, TimeToFirstChunk=${firstChunkTime}ms, TotalTime=${System.currentTimeMillis() - startTime}ms")
    }.flowOn(Dispatchers.IO)
}
