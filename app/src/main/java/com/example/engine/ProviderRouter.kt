package com.example.engine

import com.example.data.model.ChatMessage
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import com.example.data.repository.ProviderRepository
import java.net.URLEncoder

sealed class RoutingResult {
    data class Success(
        val provider: ProviderConfig,
        val targetModelId: String,
        val isImageGeneration: Boolean = false,
        val directImageResponse: String? = null
    ) : RoutingResult()

    data class ProviderDisabled(
        val providerName: String,
        val userActionRequired: String
    ) : RoutingResult()

    data class Error(val message: String) : RoutingResult()
}

class ProviderRouter(private val providerRepository: ProviderRepository) {

    private val imageKeywords = listOf(
        "generate image", "create image", "draw", "make a picture",
        "generate a picture", "generate photo", "paint a", "image of",
        "picture of", "illustration of", "render a", "artwork of",
        "draw a", "make an image", "create photo", "sketch a", "generate an image"
    )

    fun isImageRequest(prompt: String, modelId: String): Boolean {
        val lowerPrompt = prompt.lowercase().trim()
        val lowerModel = modelId.lowercase().trim()

        if (lowerModel.contains("pollinations") || lowerModel.contains("flux") || lowerModel.contains("sdxl") || lowerModel.contains("image")) {
            return true
        }

        return imageKeywords.any { lowerPrompt.contains(it) }
    }

    suspend fun routeRequest(
        prompt: String,
        messages: List<ChatMessage>,
        selectedModelId: String
    ): RoutingResult {
        val allProviders = providerRepository.getProvidersSyncList()
        val enabledProviders = allProviders.filter { it.enabled }

        val isImageReq = isImageRequest(prompt, selectedModelId)

        if (isImageReq) {
            // ROUTE TO POLLINATIONS AI FOR IMAGE GENERATION
            val pollinations = allProviders.firstOrNull { it.type == ProviderType.POLLINATIONS || it.id == "pollinations" }
                ?: ProviderConfig(
                    id = "pollinations",
                    name = "Pollinations AI",
                    type = ProviderType.POLLINATIONS,
                    apiKey = "",
                    baseUrl = ProviderType.POLLINATIONS.defaultBaseUrl,
                    selectedModel = "openai",
                    enabled = false
                )

            if (!pollinations.enabled) {
                return RoutingResult.ProviderDisabled(
                    providerName = "Pollinations AI",
                    userActionRequired = "Pollinations AI is required for image generation, but it is currently disabled in Provider Settings.\n\nPlease go to Provider & API Settings to enable Pollinations AI."
                )
            }

            // Extract prompt description
            var cleanPrompt = prompt.trim()
            imageKeywords.forEach { kw ->
                if (cleanPrompt.lowercase().startsWith(kw)) {
                    cleanPrompt = cleanPrompt.substring(kw.length).trim()
                }
            }
            if (cleanPrompt.isBlank()) cleanPrompt = prompt.trim()

            val encoded = try {
                URLEncoder.encode(cleanPrompt, "UTF-8").replace("+", "%20")
            } catch (e: Exception) {
                cleanPrompt.replace(" ", "%20")
            }

            val seed = (100000..999999).random()
            val imageUrl = "https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024&seed=$seed&nologo=true"

            val imageMarkdown = """Here is your generated image:

![Generated Image]($imageUrl)"""

            return RoutingResult.Success(
                provider = pollinations,
                targetModelId = pollinations.selectedModel,
                isImageGeneration = true,
                directImageResponse = imageMarkdown
            )
        }

        // TEXT / CODING / REASONING ROUTING
        val explicitProvider = when {
            selectedModelId.contains("/") || selectedModelId.contains("openrouter") -> {
                allProviders.firstOrNull { it.type == ProviderType.OPENROUTER || it.id == "openrouter" }
            }
            selectedModelId.contains("gemini") -> {
                allProviders.firstOrNull { it.type == ProviderType.GEMINI || it.id == "gemini" }
            }
            selectedModelId.contains("gpt") || selectedModelId.contains("openai") -> {
                allProviders.firstOrNull { it.type == ProviderType.OPENAI || it.id == "openai" }
            }
            else -> null
        }

        if (explicitProvider != null && !explicitProvider.enabled) {
            return RoutingResult.ProviderDisabled(
                providerName = explicitProvider.name,
                userActionRequired = "${explicitProvider.name} is disabled in Provider Settings.\n\nPlease enable ${explicitProvider.name} in Provider & API Settings to use '${selectedModelId}'."
            )
        }

        val targetProvider = if (explicitProvider != null && explicitProvider.enabled) {
            explicitProvider
        } else {
            // Default to OpenRouter if enabled, or first enabled text provider, or Gemini fallback
            enabledProviders.firstOrNull { it.type == ProviderType.OPENROUTER }
                ?: enabledProviders.firstOrNull { it.type != ProviderType.POLLINATIONS }
                ?: allProviders.firstOrNull { it.type == ProviderType.GEMINI }
                ?: allProviders.firstOrNull()
                ?: ProviderConfig(
                    id = "openrouter",
                    name = "OpenRouter",
                    type = ProviderType.OPENROUTER,
                    apiKey = "",
                    baseUrl = ProviderType.OPENROUTER.defaultBaseUrl,
                    selectedModel = "google/gemini-2.0-flash-lite:free",
                    enabled = true
                )
        }

        val effectiveModel = if (selectedModelId.isNotBlank() && (explicitProvider == null || explicitProvider.id == targetProvider.id)) {
            selectedModelId
        } else {
            targetProvider.selectedModel
        }

        return RoutingResult.Success(
            provider = targetProvider,
            targetModelId = effectiveModel,
            isImageGeneration = false
        )
    }
}
