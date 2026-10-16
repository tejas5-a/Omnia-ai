package com.example.data.repository

import com.example.data.local.ProviderConfigEntity
import com.example.data.local.ProviderDao
import com.example.data.local.SecurePreferencesManager
import com.example.data.model.AIModel
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import com.example.data.remote.ModelFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProviderRepository(
    private val providerDao: ProviderDao,
    private val securePrefs: SecurePreferencesManager,
    private val appPrefs: AppPreferencesRepository
) {

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
            val geminiKey = securePrefs.getDecryptedKey("gemini_api_key", com.example.BuildConfig.GEMINI_API_KEY)
            val openaiKey = securePrefs.getDecryptedKey("openai_api_key", "")
            val openrouterKey = securePrefs.getDecryptedKey("openrouter_api_key", "")
            val customKey = securePrefs.getDecryptedKey("custom_api_key", "")

            val defaults = listOf(
                ProviderConfig(
                    id = "gemini",
                    name = "Google Gemini",
                    type = ProviderType.GEMINI,
                    apiKey = geminiKey,
                    baseUrl = ProviderType.GEMINI.defaultBaseUrl,
                    selectedModel = "gemini-2.5-flash",
                    enabled = true,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.GEMINI)
                ),
                ProviderConfig(
                    id = "openai",
                    name = "OpenAI",
                    type = ProviderType.OPENAI,
                    apiKey = openaiKey,
                    baseUrl = ProviderType.OPENAI.defaultBaseUrl,
                    selectedModel = "gpt-4o",
                    enabled = false,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.OPENAI)
                ),
                ProviderConfig(
                    id = "openrouter",
                    name = "OpenRouter",
                    type = ProviderType.OPENROUTER,
                    apiKey = openrouterKey,
                    baseUrl = ProviderType.OPENROUTER.defaultBaseUrl,
                    selectedModel = "google/gemini-2.0-flash-lite:free",
                    enabled = false,
                    fetchedModels = ModelFetcher.getFallbackModels(ProviderType.OPENROUTER)
                ),
                ProviderConfig(
                    id = "custom",
                    name = "Custom / OpenAI Endpoint",
                    type = ProviderType.CUSTOM,
                    apiKey = customKey,
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
        val activeId = appPrefs.userPreferences.value.activeProviderId
        val all = providerDao.getProvidersSync().map { it.toDomainModel() }

        val active = all.firstOrNull { it.id == activeId } ?: all.firstOrNull { it.enabled }
        if (active != null) return active

        return all.firstOrNull { it.type == ProviderType.GEMINI } ?: ProviderConfig(
            id = "gemini",
            name = "Google Gemini",
            type = ProviderType.GEMINI,
            apiKey = com.example.BuildConfig.GEMINI_API_KEY,
            baseUrl = ProviderType.GEMINI.defaultBaseUrl,
            selectedModel = "gemini-2.5-flash",
            enabled = true,
            fetchedModels = ModelFetcher.getFallbackModels(ProviderType.GEMINI)
        )

    }

    suspend fun saveProvider(provider: ProviderConfig) {
        // Securely store API key in Keystore
        securePrefs.saveEncryptedKey("${provider.id}_api_key", provider.apiKey)
        providerDao.insertProvider(provider.toEntity())

        if (provider.enabled) {
            appPrefs.updateActiveProviderId(provider.id)
            appPrefs.updateSelectedModelId(provider.selectedModel)
        }
    }

    suspend fun fetchModelsForProvider(provider: ProviderConfig): List<AIModel> {
        val fetched = ModelFetcher.fetchModelsForProvider(provider)
        val updated = provider.copy(
            fetchedModels = fetched,
            lastTestedMs = System.currentTimeMillis(),
            connectionStatus = ConnectionStatus.CONNECTED
        )
        providerDao.insertProvider(updated.toEntity())
        return fetched
    }

    suspend fun addManualModel(provider: ProviderConfig, customModelId: String) {
        val existingModels = provider.fetchedModels.toMutableList()
        if (existingModels.none { it.id == customModelId }) {
            val newModel = AIModel(
                id = customModelId,
                name = customModelId,
                providerType = provider.type,
                description = "Manually Added Model ID: $customModelId"
            )
            existingModels.add(0, newModel)
        }
        val updated = provider.copy(
            selectedModel = customModelId,
            fetchedModels = existingModels,
            connectionStatus = ConnectionStatus.CONNECTED
        )
        saveProvider(updated)
        appPrefs.updateSelectedModelId(customModelId)
    }

    private fun ProviderConfigEntity.toDomainModel(): ProviderConfig {
        val typeEnum = try { ProviderType.valueOf(type) } catch (e: Exception) { ProviderType.GEMINI }
        val statusEnum = try { ConnectionStatus.valueOf(connectionStatus) } catch (e: Exception) { ConnectionStatus.UNKNOWN }

        val decryptedKey = securePrefs.getDecryptedKey("${id}_api_key", apiKey)

        val converter = com.example.data.local.RoomConverters()
        val models = converter.toAIModelList(fetchedModelsJson)

        return ProviderConfig(
            id = id,
            name = name,
            type = typeEnum,
            apiKey = decryptedKey,
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
            apiKey = "", // Secrets kept out of plain entity
            baseUrl = baseUrl,
            selectedModel = selectedModel,
            enabled = enabled,
            fetchedModelsJson = converter.fromAIModelList(fetchedModels),
            lastTestedMs = lastTestedMs,
            connectionStatus = connectionStatus.name
        )
    }
}
