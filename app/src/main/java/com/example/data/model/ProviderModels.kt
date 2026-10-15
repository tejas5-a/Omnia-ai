package com.example.data.model

enum class ProviderType(val displayName: String, val defaultBaseUrl: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/"),
    OPENAI("OpenAI", "https://api.openai.com/v1/"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/"),
    CUSTOM("Custom / OpenAI Compatible", "http://localhost:11434/v1/")
}

data class AIModel(
    val id: String,
    val name: String,
    val providerType: ProviderType,
    val description: String = "",
    val contextLength: Int = 128000,
    val isFreeTier: Boolean = false,
    val supportsThinking: Boolean = false,
    val supportsVision: Boolean = false,
    val supportsAudio: Boolean = false,
    val estimatedCostPer1kTokens: Double = 0.00015
)

data class ProviderConfig(
    val id: String,
    val name: String,
    val type: ProviderType,
    val apiKey: String,
    val baseUrl: String,
    val selectedModel: String,
    val enabled: Boolean = true,
    val fetchedModels: List<AIModel> = emptyList(),
    val lastTestedMs: Long = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.UNKNOWN
)

enum class ConnectionStatus {
    UNKNOWN,
    CHECKING,
    CONNECTED,
    ERROR
}

data class UsageMetric(
    val id: String = java.util.UUID.randomUUID().toString(),
    val providerId: String,
    val modelId: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val estimatedCostUsd: Double,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class ComparisonResult(
    val prompt: String,
    val modelA: String,
    val responseA: String,
    val latencyA: Long,
    val modelB: String,
    val responseB: String,
    val latencyB: Long,
    val reviewerAnalysis: String = ""
)
