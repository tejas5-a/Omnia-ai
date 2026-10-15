package com.example.ui.screens.workflows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import com.example.engine.WorkflowEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkflowUiState(
    val workflows: List<WorkflowDefinition> = emptyList(),
    val activeWorkflow: WorkflowDefinition? = null,
    val activeRunLog: WorkflowRunLog? = null,
    val isRunning: Boolean = false
)

class WorkflowViewModel(private val providerRepository: ProviderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState: StateFlow<WorkflowUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null
    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }
    private val workflowEngine = WorkflowEngine(llmManager)

    init {
        // Pre-populate default sample workflow
        val sampleWorkflow = WorkflowDefinition(
            name = "Full System Spec Workflow",
            description = "Sequential pipeline that analyzes requirements, writes code, and builds test cases.",
            nodes = listOf(
                WorkflowNode(
                    title = "Phase 1: Requirements Analysis",
                    stepType = StepType.PROMPT,
                    promptTemplate = "Deconstruct key requirements for an Android Jetpack Compose app."
                ),
                WorkflowNode(
                    title = "Phase 2: Code Architecture Synthesis",
                    stepType = StepType.AGENT,
                    agentType = AgentType.CODING,
                    promptTemplate = "Write type-safe clean architecture code based on Phase 1 output."
                ),
                WorkflowNode(
                    title = "Phase 3: Automated Test Generation",
                    stepType = StepType.AGENT,
                    agentType = AgentType.TESTING,
                    promptTemplate = "Generate unit test cases for the Phase 2 code implementation."
                )
            )
        )
        _uiState.update { it.copy(workflows = listOf(sampleWorkflow), activeWorkflow = sampleWorkflow) }
    }

    fun runWorkflow() {
        val wf = _uiState.value.activeWorkflow ?: return
        runJob?.cancel()
        _uiState.update { it.copy(isRunning = true) }

        runJob = viewModelScope.launch {
            workflowEngine.executeWorkflow(wf).collect { log ->
                _uiState.update { it.copy(activeRunLog = log) }
                if (log.status == WorkflowRunStatus.COMPLETED || log.status == WorkflowRunStatus.FAILED) {
                    _uiState.update { it.copy(isRunning = false) }
                }
            }
        }
    }
}
