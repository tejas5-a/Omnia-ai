package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserPreferences(
    val activeProviderId: String = "gemini",
    val selectedModelId: String = "gemini-2.5-flash",
    val isFreeModelsOnly: Boolean = false,
    val darkThemeEnabled: Boolean = true,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val defaultSystemPrompt: String = "You are OMNIA MAX, an elite AI assistant.",
    val isHighThinking: Boolean = false,
    val lastScreen: String = "chat",
    val lastActiveThreadId: String = ""
)

class AppPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("omnia_max_app_prefs", Context.MODE_PRIVATE)

    private val _userPreferences = MutableStateFlow(loadFromDisk())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private fun loadFromDisk(): UserPreferences {
        return UserPreferences(
            activeProviderId = prefs.getString("activeProviderId", "gemini") ?: "gemini",
            selectedModelId = prefs.getString("selectedModelId", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            isFreeModelsOnly = prefs.getBoolean("isFreeModelsOnly", false),
            darkThemeEnabled = prefs.getBoolean("darkThemeEnabled", true),
            themeMode = prefs.getString("themeMode", "DARK") ?: "DARK",
            defaultSystemPrompt = prefs.getString("defaultSystemPrompt", "You are OMNIA MAX, an elite AI assistant.") ?: "You are OMNIA MAX, an elite AI assistant.",
            isHighThinking = prefs.getBoolean("isHighThinking", false),
            lastScreen = prefs.getString("lastScreen", "chat") ?: "chat",
            lastActiveThreadId = prefs.getString("lastActiveThreadId", "") ?: ""
        )
    }

    fun updateActiveProviderId(providerId: String) {
        if (providerId.isBlank()) return
        prefs.edit().putString("activeProviderId", providerId).apply()
        _userPreferences.update { it.copy(activeProviderId = providerId) }
    }

    fun updateSelectedModelId(modelId: String) {
        if (modelId.isBlank()) return
        prefs.edit().putString("selectedModelId", modelId).apply()
        _userPreferences.update { it.copy(selectedModelId = modelId) }
    }

    fun updateFreeModelsOnly(enabled: Boolean) {
        prefs.edit().putBoolean("isFreeModelsOnly", enabled).apply()
        _userPreferences.update { it.copy(isFreeModelsOnly = enabled) }
    }

    fun updateDarkThemeEnabled(enabled: Boolean) {
        val mode = if (enabled) "DARK" else "LIGHT"
        prefs.edit().putBoolean("darkThemeEnabled", enabled).putString("themeMode", mode).apply()
        _userPreferences.update { it.copy(darkThemeEnabled = enabled, themeMode = mode) }
    }

    fun updateThemeMode(mode: String) {
        val isDark = when (mode) {
            "LIGHT" -> false
            else -> true
        }
        prefs.edit().putString("themeMode", mode).putBoolean("darkThemeEnabled", isDark).apply()
        _userPreferences.update { it.copy(themeMode = mode, darkThemeEnabled = isDark) }
    }

    fun updateDefaultSystemPrompt(prompt: String) {
        prefs.edit().putString("defaultSystemPrompt", prompt).apply()
        _userPreferences.update { it.copy(defaultSystemPrompt = prompt) }
    }

    fun updateHighThinking(enabled: Boolean) {
        prefs.edit().putBoolean("isHighThinking", enabled).apply()
        _userPreferences.update { it.copy(isHighThinking = enabled) }
    }

    fun updateLastScreen(screen: String) {
        if (screen == "welcome") return // Never save welcome screen
        prefs.edit().putString("lastScreen", screen).apply()
        _userPreferences.update { it.copy(lastScreen = screen) }
    }

    fun updateLastActiveThreadId(threadId: String) {
        prefs.edit().putString("lastActiveThreadId", threadId).apply()
        _userPreferences.update { it.copy(lastActiveThreadId = threadId) }
    }
}
