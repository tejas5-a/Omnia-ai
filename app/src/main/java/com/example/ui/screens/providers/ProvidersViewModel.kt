package com.example.ui.screens.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AIModel
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import com.example.data.repository.ProviderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProvidersUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val loadingProviderId: String? = null,
    val selectedProviderForEdit: ProviderConfig? = null,
    val editDialogOpen: Boolean = false,
    val statusMessage: String? = null
)

class ProvidersViewModel(private val providerRepository: ProviderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProvidersUiState())
    val uiState: StateFlow<ProvidersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.initializeDefaultProviders()
            providerRepository.allProviders.collect { list ->
                _uiState.update { it.copy(providers = list) }
            }
        }
    }

    fun fetchModelsForProvider(provider: ProviderConfig) {
        _uiState.update { it.copy(loadingProviderId = provider.id, statusMessage = "Fetching models from ${provider.name} API...") }

        viewModelScope.launch {
            try {
                val fetched = providerRepository.fetchModelsForProvider(provider)
                _uiState.update {
                    it.copy(
                        loadingProviderId = null,
                        statusMessage = "Successfully fetched ${fetched.size} models for ${provider.name}!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingProviderId = null,
                        statusMessage = "Fetch failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectModelForProvider(provider: ProviderConfig, modelId: String) {
        viewModelScope.launch {
            val updated = provider.copy(selectedModel = modelId)
            providerRepository.saveProvider(updated)
        }
    }

    fun toggleProviderEnabled(provider: ProviderConfig, enabled: Boolean) {
        viewModelScope.launch {
            // Disable all other providers if enabling this one
            if (enabled) {
                _uiState.value.providers.forEach { p ->
                    if (p.id == provider.id) {
                        providerRepository.saveProvider(p.copy(enabled = true))
                    } else {
                        providerRepository.saveProvider(p.copy(enabled = false))
                    }
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
        }
    }
}
