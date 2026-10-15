package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserPreferences(
    val activeProviderId: String = "gemini",
    val selectedModelId: String = "gemini-3.6-flash",
    val isFreeModelsOnly: Boolean = false,
    val darkThemeEnabled: Boolean = true,
    val defaultSystemPrompt: String = "You are OMNIA MAX, an elite AI assistant.",
    val isHighThinking: Boolean = false
)

class AppPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("omnia_max_app_prefs", Context.MODE_PRIVATE)

    private val _userPreferences = MutableStateFlow(loadFromDisk())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private fun loadFromDisk(): UserPreferences {
        return UserPreferences(
            activeProviderId = prefs.getString("activeProviderId", "gemini") ?: "gemini",
            selectedModelId = prefs.getString("selectedModelId", "gemini-3.6-flash") ?: "gemini-3.6-flash",
            isFreeModelsOnly = prefs.getBoolean("isFreeModelsOnly", false),
            darkThemeEnabled = prefs.getBoolean("darkThemeEnabled", true),
            defaultSystemPrompt = prefs.getString("defaultSystemPrompt", "You are OMNIA MAX, an elite AI assistant.") ?: "You are OMNIA MAX, an elite AI assistant.",
            isHighThinking = prefs.getBoolean("isHighThinking", false)
        )
    }

    fun updateActiveProviderId(providerId: String) {
        prefs.edit().putString("activeProviderId", providerId).apply()
        _userPreferences.update { it.copy(activeProviderId = providerId) }
    }

    fun updateSelectedModelId(modelId: String) {
        prefs.edit().putString("selectedModelId", modelId).apply()
        _userPreferences.update { it.copy(selectedModelId = modelId) }
    }

    fun updateFreeModelsOnly(enabled: Boolean) {
        prefs.edit().putBoolean("isFreeModelsOnly", enabled).apply()
        _userPreferences.update { it.copy(isFreeModelsOnly = enabled) }
    }

    fun updateDarkThemeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("darkThemeEnabled", enabled).apply()
        _userPreferences.update { it.copy(darkThemeEnabled = enabled) }
    }

    fun updateDefaultSystemPrompt(prompt: String) {
        prefs.edit().putString("defaultSystemPrompt", prompt).apply()
        _userPreferences.update { it.copy(defaultSystemPrompt = prompt) }
    }

    fun updateHighThinking(enabled: Boolean) {
        prefs.edit().putBoolean("isHighThinking", enabled).apply()
        _userPreferences.update { it.copy(isHighThinking = enabled) }
    }
}
