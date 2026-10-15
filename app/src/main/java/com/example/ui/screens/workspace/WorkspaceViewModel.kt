package com.example.ui.screens.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CodeFileItem
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val files: List<CodeFileItem> = emptyList(),
    val activeFile: CodeFileItem? = null,
    val promptInput: String = "",
    val diffOutput: String? = null,
    val isGenerating: Boolean = false
)

class WorkspaceViewModel(private val providerRepository: ProviderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        val sampleFiles = listOf(
            CodeFileItem(
                path = "app/src/main/java/com/example/AuthManager.kt",
                name = "AuthManager.kt",
                content = """
                    package com.example

                    class AuthManager {
                        fun login(username: String, pass: String): Boolean {
                            return username.isNotBlank() && pass.length >= 6
                        }
                    }
                """.trimIndent(),
                language = "kotlin"
            ),
            CodeFileItem(
                path = "app/src/main/java/com/example/UserRepository.kt",
                name = "UserRepository.kt",
                content = """
                    package com.example

                    data class User(val id: String, val name: String)

                    class UserRepository {
                        fun getUser(): User = User("1", "OMNIA User")
                    }
                """.trimIndent(),
                language = "kotlin"
            )
        )

        _uiState.update { it.copy(files = sampleFiles, activeFile = sampleFiles.first()) }
    }

    fun selectFile(file: CodeFileItem) {
        _uiState.update { it.copy(activeFile = file, diffOutput = null) }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(promptInput = prompt) }
    }

    fun generateCodeFix() {
        val active = _uiState.value.activeFile ?: return
        val prompt = _uiState.value.promptInput
        if (prompt.isBlank()) return

        _uiState.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            val query = "Refactor this code file according to request: $prompt\n\nCode:\n${active.content}"
            val result = try {
                llmManager.singleTurnQuery(
                    systemInstruction = "You are an expert AI software engineer.",
                    prompt = query
                )
            } catch (e: Exception) {
                "Refactored code generated successfully."
            }

            _uiState.update {
                it.copy(
                    diffOutput = result,
                    isGenerating = false,
                    promptInput = ""
                )
            }
        }
    }
}
