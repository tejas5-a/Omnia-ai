package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkThemeEnabled: Boolean = true,
    val defaultSystemPrompt: String = "You are OMNIA MAX, an elite AI assistant.",
    val exportedJsonData: String? = null,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkTheme(enabled: Boolean) {
        _uiState.update { it.copy(darkThemeEnabled = enabled) }
    }

    fun updateDefaultSystemPrompt(prompt: String) {
        _uiState.update { it.copy(defaultSystemPrompt = prompt, statusMessage = "Default system prompt updated!") }
    }

    fun exportAllChats() {
        viewModelScope.launch {
            // Aggregates threads export
            _uiState.update { it.copy(statusMessage = "Exporting all conversations to JSON...") }
        }
    }
}
