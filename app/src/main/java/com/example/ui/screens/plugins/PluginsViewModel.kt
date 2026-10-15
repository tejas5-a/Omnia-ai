package com.example.ui.screens.plugins

import androidx.lifecycle.ViewModel
import com.example.data.model.PluginCategory
import com.example.data.model.PluginTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PluginsUiState(
    val plugins: List<PluginTool> = emptyList()
)

class PluginsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    init {
        val samplePlugins = listOf(
            PluginTool(
                id = "web_search",
                name = "Web Research & Grounding",
                description = "Fetches real-time web references, documentation, and technical news.",
                category = PluginCategory.RESEARCH,
                isEnabled = true
            ),
            PluginTool(
                id = "code_linter",
                name = "Kotlin AST Code Linter",
                description = "Performs static code checks and flags potential bugs.",
                category = PluginCategory.CODING,
                isEnabled = true
            ),
            PluginTool(
                id = "doc_parser",
                name = "PDF & Document Text Extractor",
                description = "Extracts structured text and tables from local documents.",
                category = PluginCategory.DOCUMENT,
                isEnabled = true
            ),
            PluginTool(
                id = "sec_auditor",
                name = "Security Vulnerability Scanner",
                description = "Scans code snippets for hardcoded secrets and OWASP risks.",
                category = PluginCategory.UTILITY,
                isEnabled = true
            )
        )
        _uiState.update { it.copy(plugins = samplePlugins) }
    }

    fun togglePlugin(id: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.plugins.map { p ->
                if (p.id == id) p.copy(isEnabled = enabled) else p
            }
            state.copy(plugins = updated)
        }
    }
}
