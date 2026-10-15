package com.example.data.model

enum class PluginCategory {
    RESEARCH,
    CODING,
    DOCUMENT,
    PRODUCTIVITY,
    UTILITY
}

data class PluginTool(
    val id: String,
    val name: String,
    val description: String,
    val category: PluginCategory,
    val isEnabled: Boolean = true,
    val requiredPermissions: List<String> = emptyList()
)
