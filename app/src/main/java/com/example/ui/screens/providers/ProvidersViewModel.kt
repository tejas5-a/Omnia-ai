package com.example.ui.screens.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.data.repository.AppPreferencesRepository
import com.example.data.repository.ProviderRepository
import com.example.data.remote.ModelFetcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProvidersUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val loadingProviderId: String? = null,
    val testingProviderId: String? = null,
    val selectedProviderForEdit: ProviderConfig? = null,
    val editDialogOpen: Boolean = false,
    val manualModelDialogOpen: Boolean = false,
    val manualModelProvider: ProviderConfig? = null,
    val isFreeModelsOnly: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class ProvidersViewModel(
    private val providerRepository: ProviderRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProvidersUiState())
    val uiState: StateFlow<ProvidersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.initializeDefaultProviders()

            combine(providerRepository.allProviders, appPrefs.userPreferences) { providersList, prefs ->
                Pair(providersList, prefs)
            }.collect { (providersList, prefs) ->
                _uiState.update {
                    it.copy(
                        providers = providersList,
                        isFreeModelsOnly = prefs.isFreeModelsOnly
                    )
                }
            }
        }
    }

    fun fetchModelsForProvider(provider: ProviderConfig) {
        _uiState.update {
            it.copy(
                loadingProviderId = provider.id,
                statusMessage = "Fetching active models from ${provider.name}...",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val fetched = providerRepository.fetchModelsForProvider(provider)
                _uiState.update {
                    it.copy(
                        loadingProviderId = null,
                        statusMessage = "Successfully retrieved ${fetched.size} verified models for ${provider.name}!",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingProviderId = null,
                        statusMessage = null,
                        errorMessage = "Fetch Failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun testConnectionForProvider(provider: ProviderConfig) {
        _uiState.update {
            it.copy(
                testingProviderId = provider.id,
                statusMessage = "Testing API connection to ${provider.name}...",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val models = ModelFetcher.fetchModelsForProvider(provider)
                val updated = provider.copy(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    lastTestedMs = System.currentTimeMillis()
                )
                providerRepository.saveProvider(updated)
                _uiState.update {
                    it.copy(
                        testingProviderId = null,
                        statusMessage = "Connection verified! ${provider.name} responded with ${models.size} models.",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                val updated = provider.copy(
                    connectionStatus = ConnectionStatus.ERROR,
                    lastTestedMs = System.currentTimeMillis()
                )
                providerRepository.saveProvider(updated)
                _uiState.update {
                    it.copy(
                        testingProviderId = null,
                        statusMessage = null,
                        errorMessage = "Connection Test Failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectModelForProvider(provider: ProviderConfig, modelId: String) {
        viewModelScope.launch {
            val trimmed = modelId.trim()
            if (trimmed.isEmpty()) return@launch
            providerRepository.addManualModel(provider, trimmed)
            appPrefs.updateSelectedModelId(trimmed)
            _uiState.update { it.copy(statusMessage = "Active model set to: $trimmed") }
        }
    }

    fun toggleFreeModelsOnly(enabled: Boolean) {
        appPrefs.updateFreeModelsOnly(enabled)
    }

    fun toggleProviderEnabled(provider: ProviderConfig, enabled: Boolean) {
        viewModelScope.launch {
            val updated = provider.copy(enabled = enabled)
            providerRepository.saveProvider(updated)
            if (enabled && provider.type != com.example.data.model.ProviderType.POLLINATIONS) {
                appPrefs.updateActiveProviderId(provider.id)
                appPrefs.updateSelectedModelId(provider.selectedModel)
            }
            val stateMsg = if (enabled) "${provider.name} is now Enabled." else "${provider.name} is now Disabled."
            _uiState.update { it.copy(statusMessage = stateMsg, errorMessage = null) }
        }
    }

    fun openEditDialog(provider: ProviderConfig) {
        _uiState.update { it.copy(selectedProviderForEdit = provider, editDialogOpen = true) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(editDialogOpen = false, selectedProviderForEdit = null) }
    }

    fun openManualModelDialog(provider: ProviderConfig) {
        _uiState.update { it.copy(manualModelProvider = provider, manualModelDialogOpen = true) }
    }

    fun closeManualModelDialog() {
        _uiState.update { it.copy(manualModelDialogOpen = false, manualModelProvider = null) }
    }

    fun saveProviderDetails(
        providerId: String,
        name: String,
        apiKey: String,
        baseUrl: String
    ) {
        val existing = _uiState.value.providers.firstOrNull { it.id == providerId } ?: return
        viewModelScope.launch {
            val updated = existing.copy(
                name = name,
                apiKey = apiKey,
                baseUrl = baseUrl,
                connectionStatus = ConnectionStatus.UNKNOWN
            )
            providerRepository.saveProvider(updated)
            closeEditDialog()
            _uiState.update { it.copy(statusMessage = "Updated credentials for $name.") }
        }
    }

    fun saveManualModelId(provider: ProviderConfig, customModelId: String) {
        if (customModelId.isBlank()) return
        viewModelScope.launch {
            providerRepository.addManualModel(provider, customModelId.trim())
            closeManualModelDialog()
            _uiState.update { it.copy(statusMessage = "Manually added model ID '$customModelId' and set as active.") }
        }
    }
}
