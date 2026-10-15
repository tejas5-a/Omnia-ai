package com.example.data.repository

import com.example.data.local.ProviderConfigEntity
import com.example.data.local.ProviderDao
import com.example.data.model.AIModel
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import com.example.data.remote.ModelFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProviderRepository(private val providerDao: ProviderDao) {

    val allProviders: Flow<List<ProviderConfig>> = providerDao.getAllProviders().map { entities ->
        if (entities.isEmpty()) {
            emptyList()
        } else {
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun initializeDefaultProviders() {
        val existing = providerDao.getProvidersSync()
        if (existing.isEmpty()) {
            val geminiKey = com.example.BuildConfig.GEMINI_API_KEY
            val defaults = listOf(
                ProviderConfig(
                    id = "gemini",
                    name = "Google Gemini",
                    type = ProviderType.GEMINI,
                    apiKey = geminiKey,
                    baseUrl = ProviderType.GEMINI.defaultBaseUrl,
                    selectedModel = "gemini-3.6-flash",
                    enabled = true,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.GEMINI)
                ),
                ProviderConfig(
                    id = "openai",
                    name = "OpenAI",
                    type = ProviderType.OPENAI,
                    apiKey = "",
                    baseUrl = ProviderType.OPENAI.defaultBaseUrl,
                    selectedModel = "gpt-4o",
                    enabled = false,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.OPENAI)
                ),
                ProviderConfig(
                    id = "openrouter",
                    name = "OpenRouter",
                    type = ProviderType.OPENROUTER,
                    apiKey = "",
                    baseUrl = ProviderType.OPENROUTER.defaultBaseUrl,
                    selectedModel = "google/gemini-2.0-flash-lite:free",
                    enabled = false,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.OPENROUTER)
                ),
                ProviderConfig(
                    id = "custom",
                    name = "Custom / OpenAI Endpoint",
                    type = ProviderType.CUSTOM,
                    apiKey = "",
                    baseUrl = ProviderType.CUSTOM.defaultBaseUrl,
                    selectedModel = "llama3",
                    enabled = false,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.CUSTOM)
                )
            )

            defaults.forEach { config ->
                providerDao.insertProvider(config.toEntity())
            }
        }
    }

    suspend fun getActiveProvider(): ProviderConfig {
        val all = providerDao.getProvidersSync().map { it.toDomainModel() }
        val enabled = all.firstOrNull { it.enabled }
        if (enabled != null) return enabled

        // Fallback to Gemini if none enabled
        return all.firstOrNull { it.type == ProviderType.GEMINI } ?: ProviderConfig(
            id = "gemini",
            name = "Google Gemini",
            type = ProviderType.GEMINI,
            apiKey = com.example.BuildConfig.GEMINI_API_KEY,
            baseUrl = ProviderType.GEMINI.defaultBaseUrl,
            selectedModel = "gemini-3.6-flash",
            enabled = true,
            fetchedModels = ModelFetcher.getFallbackModels(ProviderType.GEMINI)
        )
    }

    suspend fun saveProvider(provider: ProviderConfig) {
        providerDao.insertProvider(provider.toEntity())
    }

    suspend fun fetchModelsForProvider(provider: ProviderConfig): List<AIModel> {
        val fetched = ModelFetcher.fetchModelsForProvider(provider)
        val updated = provider.copy(
            fetchedModels = fetched,
            lastTestedMs = System.currentTimeMillis(),
            connectionStatus = if (fetched.isNotEmpty()) ConnectionStatus.CONNECTED else ConnectionStatus.ERROR
        )
        providerDao.insertProvider(updated.toEntity())
        return fetched
    }

    private fun ProviderConfigEntity.toDomainModel(): ProviderConfig {
        val typeEnum = try { ProviderType.valueOf(type) } catch (e: Exception) { ProviderType.GEMINI }
        val statusEnum = try { ConnectionStatus.valueOf(connectionStatus) } catch (e: Exception) { ConnectionStatus.UNKNOWN }

        // Parse fetched models
        val converter = com.example.data.local.RoomConverters()
        val models = converter.toAIModelList(fetchedModelsJson)

        return ProviderConfig(
            id = id,
            name = name,
            type = typeEnum,
            apiKey = apiKey,
            baseUrl = baseUrl,
            selectedModel = selectedModel,
            enabled = enabled,
            fetchedModels = if (models.isNotEmpty()) models else ModelFetcher.getFallbackModels(typeEnum),
            lastTestedMs = lastTestedMs,
            connectionStatus = statusEnum
        )
    }

    private fun ProviderConfig.toEntity(): ProviderConfigEntity {
        val converter = com.example.data.local.RoomConverters()
        return ProviderConfigEntity(
            id = id,
            name = name,
            type = type.name,
            apiKey = apiKey,
            baseUrl = baseUrl,
            selectedModel = selectedModel,
            enabled = enabled,
            fetchedModelsJson = converter.fromAIModelList(fetchedModels),
            lastTestedMs = lastTestedMs,
            connectionStatus = connectionStatus.name
        )
    }
}
