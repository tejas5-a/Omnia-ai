package com.example.data.remote

import com.example.data.model.AIModel
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class FetchException(message: String) : Exception(message)

object ModelFetcher {

    suspend fun fetchModelsForProvider(config: ProviderConfig): List<AIModel> = withContext(Dispatchers.IO) {
        val client = ApiClient.okHttpClient
        val modelsList = mutableListOf<AIModel>()

        when (config.type) {
            ProviderType.GEMINI -> {
                val key = config.apiKey.ifBlank { com.example.BuildConfig.GEMINI_API_KEY }
                if (key.isBlank()) {
                    throw FetchException("Google Gemini API key is missing. Please set your key in the Secrets panel or Provider Hub.")
                }
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
                val request = Request.Builder().url(url).get().build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        throw FetchException("Gemini API Error (HTTP ${response.code}): $errBody")
                    }
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val modelsArray = json.optJSONArray("models") ?: org.json.JSONArray()

                    for (i in 0 until modelsArray.length()) {
                        val obj = modelsArray.getJSONObject(i)
                        val rawName = obj.optString("name", "") // e.g. "models/gemini-1.5-flash"
                        val cleanId = rawName.removePrefix("models/")
                        val displayName = obj.optString("displayName", cleanId)
                        val description = obj.optString("description", "")
                        val inputTokenLimit = obj.optInt("inputTokenLimit", 128000)

                        if (cleanId.contains("gemini") || cleanId.contains("veo") || cleanId.contains("embedding")) {
                            modelsList.add(
                                AIModel(
                                    id = cleanId,
                                    name = displayName,
                                    providerType = ProviderType.GEMINI,
                                    description = description,
                                    contextLength = inputTokenLimit,
                                    isFreeTier = true,
                                    supportsThinking = cleanId.contains("argon") || cleanId.contains("thinking") || cleanId.contains("pro") || cleanId.contains("flash"),
                                    supportsVision = cleanId.contains("flash") || cleanId.contains("image")
                                )
                            )
                        }
                    }
                }
            }

            ProviderType.OPENAI -> {
                val baseUrl = config.baseUrl.ifBlank { ProviderType.OPENAI.defaultBaseUrl }.trimEnd('/')
                val url = "$baseUrl/models"
                val requestBuilder = Request.Builder().url(url).get()
                if (config.apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
                }
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        throw FetchException("OpenAI API Error (HTTP ${response.code}): $errBody")
                    }
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val modelId = obj.optString("id", "")
                        if (modelId.isNotBlank()) {
                            modelsList.add(
                                AIModel(
                                    id = modelId,
                                    name = modelId,
                                    providerType = ProviderType.OPENAI,
                                    description = "OpenAI Model ($modelId)",
                                    contextLength = 128000,
                                    supportsVision = modelId.contains("gpt-4o") || modelId.contains("vision")
                                )
                            )
                        }
                    }
                }
            }

            ProviderType.OPENROUTER -> {
                val url = "https://openrouter.ai/api/v1/models"
                val requestBuilder = Request.Builder().url(url).get()
                if (config.apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
                }
                requestBuilder.addHeader("HTTP-Referer", "https://github.com/omnia-max")
                requestBuilder.addHeader("X-Title", "OMNIA MAX")

                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        throw FetchException("OpenRouter API Error (HTTP ${response.code}): $errBody")
                    }
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val modelId = obj.optString("id", "")
                        val modelName = obj.optString("name", modelId)
                        val ctxLen = obj.optInt("context_length", 128000)

                        val pricingObj = obj.optJSONObject("pricing")
                        val promptCost = pricingObj?.optDouble("prompt", 0.0) ?: 0.0

                        val isFree = modelId.contains(":free") || promptCost == 0.0

                        val architecture = obj.optJSONObject("architecture")
                        val modality = architecture?.optString("modality", "") ?: ""
                        val hasVision = modality.contains("image") || modelId.contains("vision") || modelId.contains("gemini")

                        if (modelId.isNotBlank()) {
                            modelsList.add(
                                AIModel(
                                    id = modelId,
                                    name = modelName,
                                    providerType = ProviderType.OPENROUTER,
                                    description = "ID: $modelId",
                                    contextLength = ctxLen,
                                    isFreeTier = isFree,
                                    supportsThinking = modelId.contains("r1") || modelId.contains("claude") || modelId.contains("thinking"),
                                    supportsVision = hasVision,
                                    estimatedCostPer1kTokens = promptCost * 1000
                                )
                            )
                        }
                    }
                }
            }

            ProviderType.CUSTOM -> {
                val baseUrl = config.baseUrl.ifBlank { ProviderType.CUSTOM.defaultBaseUrl }.trimEnd('/')
                val url = if (baseUrl.endsWith("/models")) baseUrl else "$baseUrl/models"
                val requestBuilder = Request.Builder().url(url).get()
                if (config.apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
                }
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        throw FetchException("Custom API Error (HTTP ${response.code}): $errBody")
                    }
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val modelId = obj.optString("id", "")
                        if (modelId.isNotBlank()) {
                            modelsList.add(
                                AIModel(
                                    id = modelId,
                                    name = modelId,
                                    providerType = ProviderType.CUSTOM,
                                    description = "Custom Model ID: $modelId"
                                )
                            )
                        }
                    }
                }
            }
        }

        if (modelsList.isEmpty()) {
            throw FetchException("No models returned by ${config.name} API endpoint.")
        }

        return@withContext modelsList
    }

    fun getFallbackModels(type: ProviderType): List<AIModel> {
        return when (type) {
            ProviderType.GEMINI -> listOf(
                AIModel("gemini-2.5-flash", "Gemini 2.5 Flash (Recommended)", ProviderType.GEMINI, "ID: gemini-2.5-flash", 1000000, isFreeTier = true, supportsThinking = true, supportsVision = true),
                AIModel("gemini-2.5-pro", "Gemini 2.5 Pro (Deep Reasoning)", ProviderType.GEMINI, "ID: gemini-2.5-pro", 2000000, isFreeTier = true, supportsThinking = true, supportsVision = true),
                AIModel("gemini-2.0-flash", "Gemini 2.0 Flash", ProviderType.GEMINI, "ID: gemini-2.0-flash", 1000000, isFreeTier = true, supportsThinking = false),
                AIModel("gemini-1.5-flash", "Gemini 1.5 Flash", ProviderType.GEMINI, "ID: gemini-1.5-flash", 1000000, isFreeTier = true, supportsVision = true)
            )

            ProviderType.OPENAI -> listOf(
                AIModel("gpt-4o", "GPT-4o (Omni)", ProviderType.OPENAI, "ID: gpt-4o", 128000, supportsVision = true),
                AIModel("gpt-4o-mini", "GPT-4o Mini", ProviderType.OPENAI, "ID: gpt-4o-mini", 128000, supportsVision = true),
                AIModel("o3-mini", "o3-mini Reasoning", ProviderType.OPENAI, "ID: o3-mini", 200000, supportsThinking = true)
            )
            ProviderType.OPENROUTER -> listOf(
                AIModel("google/gemini-2.0-flash-lite:free", "Gemini 2.0 Flash Lite (Free)", ProviderType.OPENROUTER, "ID: google/gemini-2.0-flash-lite:free", 1000000, isFreeTier = true),
                AIModel("deepseek/deepseek-r1:free", "DeepSeek R1 Reasoning (Free)", ProviderType.OPENROUTER, "ID: deepseek/deepseek-r1:free", 64000, isFreeTier = true, supportsThinking = true),
                AIModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (Free)", ProviderType.OPENROUTER, "ID: meta-llama/llama-3.3-70b-instruct:free", 128000, isFreeTier = true)
            )
            ProviderType.CUSTOM -> listOf(
                AIModel("llama3", "Llama 3 Local / Custom", ProviderType.CUSTOM, "ID: llama3", 8192),
                AIModel("mistral", "Mistral Local / Custom", ProviderType.CUSTOM, "ID: mistral", 8192)
            )
        }
    }
}
