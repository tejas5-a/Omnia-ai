package com.example.engine

import com.example.data.model.AIModel
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType

data class RoutingDecision(
    val selectedModel: AIModel,
    val selectedProvider: ProviderConfig,
    val explanation: String
)

object LLMRouter {

    fun routeQuery(
        prompt: String,
        providers: List<ProviderConfig>,
        isFreeModelsOnly: Boolean = false,
        isAutoMode: Boolean = true
    ): RoutingDecision {
        val enabledProviders = providers.filter { it.enabled && it.apiKey.isNotBlank() }
            .ifEmpty { providers.filter { it.type == ProviderType.GEMINI } }

        val activeProvider = enabledProviders.firstOrNull() ?: providers.first()

        val candidateModels = activeProvider.fetchedModels.ifEmpty {
            com.example.data.remote.ModelFetcher.getFallbackModels(activeProvider.type)
        }

        if (isFreeModelsOnly) {
            val freeModel = candidateModels.firstOrNull { it.isFreeTier } ?: candidateModels.first()
            return RoutingDecision(
                selectedModel = freeModel,
                selectedProvider = activeProvider,
                explanation = "Free Models Only filter applied: Selected ${freeModel.name} on ${activeProvider.name}."
            )
        }

        if (!isAutoMode) {
            val selected = candidateModels.firstOrNull { it.id == activeProvider.selectedModel }
                ?: candidateModels.first()
            return RoutingDecision(
                selectedModel = selected,
                selectedProvider = activeProvider,
                explanation = "Manual selection active: ${selected.name}."
            )
        }

        // Auto Intent Analysis
        val lower = prompt.lowercase()
        return when {
            lower.contains("code") || lower.contains("kotlin") || lower.contains("function") || lower.contains("bug") || lower.contains("class") -> {
                val codeModel = candidateModels.firstOrNull { it.id.contains("pro") || it.id.contains("gpt-4o") || it.id.contains("r1") }
                    ?: candidateModels.first()
                RoutingDecision(
                    selectedModel = codeModel,
                    selectedProvider = activeProvider,
                    explanation = "Intent: High Complexity Coding → Selected ${codeModel.name} for deep code synthesis."
                )
            }

            lower.contains("think") || lower.contains("reason") || lower.contains("math") || lower.contains("logic") || lower.contains("proof") -> {
                val reasoningModel = candidateModels.firstOrNull { it.supportsThinking || it.id.contains("argon") || it.id.contains("o3") }
                    ?: candidateModels.first()
                RoutingDecision(
                    selectedModel = reasoningModel,
                    selectedProvider = activeProvider,
                    explanation = "Intent: High Reasoning & STEM → Selected ${reasoningModel.name} with deep reasoning."
                )
            }

            else -> {
                val fastModel = candidateModels.firstOrNull { it.id.contains("flash") || it.id.contains("mini") }
                    ?: candidateModels.first()
                RoutingDecision(
                    selectedModel = fastModel,
                    selectedProvider = activeProvider,
                    explanation = "Intent: Conversational Q&A → Selected low-latency ${fastModel.name}."
                )
            }
        }
    }
}
