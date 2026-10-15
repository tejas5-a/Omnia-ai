package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppPreferencesRepository
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
    val isFreeModelsOnly: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPrefs.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        darkThemeEnabled = prefs.darkThemeEnabled,
                        defaultSystemPrompt = prefs.defaultSystemPrompt,
                        isFreeModelsOnly = prefs.isFreeModelsOnly
                    )
                }
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        appPrefs.updateDarkThemeEnabled(enabled)
    }

    fun toggleFreeModelsOnly(enabled: Boolean) {
        appPrefs.updateFreeModelsOnly(enabled)
    }

    fun updateDefaultSystemPrompt(prompt: String) {
        appPrefs.updateDefaultSystemPrompt(prompt)
        _uiState.update { it.copy(statusMessage = "Default system prompt saved persistently.") }
    }
}
