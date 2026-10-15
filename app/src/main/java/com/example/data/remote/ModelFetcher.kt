package com.example.data.remote

import com.example.data.model.AIModel
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

object ModelFetcher {

    suspend fun fetchModelsForProvider(config: ProviderConfig): List<AIModel> = withContext(Dispatchers.IO) {
        val client = ApiClient.okHttpClient
        val modelsList = mutableListOf<AIModel>()

        try {
            when (config.type) {
                ProviderType.GEMINI -> {
                    val key = config.apiKey.ifBlank { com.example.BuildConfig.GEMINI_API_KEY }
                    val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
                    val request = Request.Builder().url(url).get().build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("HTTP ${response.code}: ${response.message}")
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
                            throw Exception("HTTP ${response.code}: ${response.message}")
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
                    client.newCall(requestBuilder.build()).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("HTTP ${response.code}: ${response.message}")
                        }
                        val bodyString = response.body?.string() ?: ""
                        val json = JSONObject(bodyString)
                        val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()

                        for (i in 0 until dataArray.length()) {
                            val obj = dataArray.getJSONObject(i)
                            val modelId = obj.optString("id", "")
                            val modelName = obj.optString("name", modelId)
                            val ctxLen = obj.optInt("context_length", 128000)

                            if (modelId.isNotBlank()) {
                                modelsList.add(
                                    AIModel(
                                        id = modelId,
                                        name = modelName,
                                        providerType = ProviderType.OPENROUTER,
                                        description = "OpenRouter model",
                                        contextLength = ctxLen,
                                        isFreeTier = modelId.contains(":free"),
                                        supportsThinking = modelId.contains("r1") || modelId.contains("claude")
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
                            throw Exception("HTTP ${response.code}: ${response.message}")
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
                                        description = "Custom endpoint model"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // If fetch fails (e.g. no key, network offline), return fallback models for that provider
            return@withContext getFallbackModels(config.type)
        }

        if (modelsList.isEmpty()) {
            return@withContext getFallbackModels(config.type)
        }

        return@withContext modelsList
    }

    fun getFallbackModels(type: ProviderType): List<AIModel> {
        return when (type) {
            ProviderType.GEMINI -> listOf(
                AIModel("gemini-3.6-flash", "Gemini 3.6 Flash (Recommended)", ProviderType.GEMINI, "Fast, intelligent multimodal reasoning", 1000000, isFreeTier = true, supportsThinking = true, supportsVision = true),
                AIModel("gemini-3.1-pro-preview", "Gemini 3.1 Pro Preview", ProviderType.GEMINI, "Advanced reasoning & deep code synthesis", 2000000, isFreeTier = true, supportsThinking = true, supportsVision = true),
                AIModel("gemini-3.5-flash", "Gemini 3.5 Flash", ProviderType.GEMINI, "General purpose text and code task model", 1000000, isFreeTier = true, supportsThinking = false),
                AIModel("gemini-2.5-flash", "Gemini 2.5 Flash", ProviderType.GEMINI, "Ultra-fast response time model", 1000000, isFreeTier = true, supportsVision = true)
            )
            ProviderType.OPENAI -> listOf(
                AIModel("gpt-4o", "GPT-4o (Omni)", ProviderType.OPENAI, "High intelligence flagship model", 128000, supportsVision = true),
                AIModel("gpt-4o-mini", "GPT-4o Mini", ProviderType.OPENAI, "Fast, lightweight model for everyday tasks", 128000, supportsVision = true),
                AIModel("o3-mini", "o3-mini Reasoning", ProviderType.OPENAI, "High reasoning STEM and coding model", 200000, supportsThinking = true)
            )
            ProviderType.OPENROUTER -> listOf(
                AIModel("google/gemini-2.0-flash-lite:free", "Gemini 2.0 Flash Lite (Free)", ProviderType.OPENROUTER, "Free tier OpenRouter model", 1000000, isFreeTier = true),
                AIModel("deepseek/deepseek-r1:free", "DeepSeek R1 Reasoning (Free)", ProviderType.OPENROUTER, "Free open reasoning model", 64000, isFreeTier = true, supportsThinking = true),
                AIModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (Free)", ProviderType.OPENROUTER, "Free high capacity open model", 128000, isFreeTier = true)
            )
            ProviderType.CUSTOM -> listOf(
                AIModel("llama3", "Llama 3 Local / Custom", ProviderType.CUSTOM, "Local Ollama / vLLM endpoint", 8192),
                AIModel("mistral", "Mistral Local / Custom", ProviderType.CUSTOM, "Local mistral endpoint", 8192)
            )
        }
    }
}
