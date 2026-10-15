package com.example.ui.screens.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.data.repository.AppPreferencesRepository
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProvidersUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val loadingProviderId: String? = null,
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

    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            providerRepository.initializeDefaultProviders()

            // Combine provider data with user preferences
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

    fun selectModelForProvider(provider: ProviderConfig, modelId: String) {
        viewModelScope.launch {
            val updated = provider.copy(selectedModel = modelId)
            providerRepository.saveProvider(updated)
            appPrefs.updateSelectedModelId(modelId)
            _uiState.update { it.copy(statusMessage = "Active model set to: $modelId") }
        }
    }

    fun toggleFreeModelsOnly(enabled: Boolean) {
        appPrefs.updateFreeModelsOnly(enabled)
    }

    fun toggleProviderEnabled(provider: ProviderConfig, enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                _uiState.value.providers.forEach { p ->
                    providerRepository.saveProvider(p.copy(enabled = p.id == provider.id))
                }
            } else {
                providerRepository.saveProvider(provider.copy(enabled = false))
            }
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
