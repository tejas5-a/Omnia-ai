package com.example.ui.screens.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgentRunStatus
import com.example.data.model.AgentTaskRun
import com.example.data.repository.AgentRepository
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import com.example.engine.OmniaMultiAgentEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AgentsUiState(
    val taskHistory: List<AgentTaskRun> = emptyList(),
    val currentActiveRun: AgentTaskRun? = null,
    val isEngineRunning: Boolean = false,
    val userPromptInput: String = ""
)

class AgentsViewModel(
    private val agentRepository: AgentRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    private var pipelineJob: Job? = null
    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }
    private val engine = OmniaMultiAgentEngine(llmManager)

    init {
        viewModelScope.launch {
            agentRepository.allTaskRuns.collect { list ->
                _uiState.update { it.copy(taskHistory = list) }
            }
        }
    }

    fun updatePromptInput(input: String) {
        _uiState.update { it.copy(userPromptInput = input) }
    }

    fun executeAgentPipeline(promptText: String? = null) {
        val query = promptText ?: _uiState.value.userPromptInput
        if (query.isBlank()) return

        pipelineJob?.cancel()
        _uiState.update { it.copy(isEngineRunning = true, userPromptInput = "") }

        pipelineJob = viewModelScope.launch {
            engine.executePipeline(query).collect { runState ->
                _uiState.update { it.copy(currentActiveRun = runState) }
                agentRepository.saveTaskRun(runState)

                if (runState.status == AgentRunStatus.COMPLETED || runState.status == AgentRunStatus.FAILED) {
                    _uiState.update { it.copy(isEngineRunning = false) }
                }
            }
        }
    }

    fun cancelPipeline() {
        pipelineJob?.cancel()
        _uiState.update { it.copy(isEngineRunning = false) }
    }

    fun selectHistoryRun(run: AgentTaskRun) {
        _uiState.update { it.copy(currentActiveRun = run) }
    }
}
