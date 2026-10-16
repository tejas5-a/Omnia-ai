package com.example.ui.screens.automations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.automation.AutomationEngine
import com.example.automation.AutomationRepository
import com.example.automation.AutomationScheduler
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AutomationsViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AutomationRepository(application)
    private val engine = AutomationEngine(application, repository)
    private val scheduler = AutomationScheduler(application)

    val automations: StateFlow<List<AutomationWorkflow>> = repository.allAutomations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runLogs: StateFlow<List<AutomationRunLog>> = repository.allRunLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWorkflow = MutableStateFlow<AutomationWorkflow?>(null)
    val selectedWorkflow: StateFlow<AutomationWorkflow?> = _selectedWorkflow.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _lastExecutionSummary = MutableStateFlow<String?>(null)
    val lastExecutionSummary: StateFlow<String?> = _lastExecutionSummary.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultTemplatesIfEmpty()
        }
    }

    fun toggleEnabled(workflow: AutomationWorkflow) {
        viewModelScope.launch {
            val newState = !workflow.isEnabled
            repository.toggleEnabled(workflow.id, newState)
            if (workflow.triggerType == TriggerType.SCHEDULED) {
                if (newState) {
                    scheduler.scheduleWorkflowAlarm(workflow.id, System.currentTimeMillis() + 60_000)
                } else {
                    scheduler.cancelWorkflowAlarm(workflow.id)
                }
            }
        }
    }

    fun deleteWorkflow(workflowId: String) {
        viewModelScope.launch {
            repository.deleteAutomation(workflowId)
        }
    }

    fun executeWorkflow(workflow: AutomationWorkflow, runtimeInputs: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            _isExecuting.value = true
            val log = engine.executeWorkflow(workflow, runtimeInputs)
            _lastExecutionSummary.value = log.outputSummary
            _isExecuting.value = false
        }
    }

    fun selectWorkflowForEdit(workflow: AutomationWorkflow?) {
        _selectedWorkflow.value = workflow
    }

    fun createNewWorkflow(targetApp: CrossAppTarget = CrossAppTarget.SYSTEM) {
        val newWf = AutomationWorkflow(
            id = UUID.randomUUID().toString(),
            name = "New Automation Workflow",
            description = "Custom cross-app automation for ${targetApp.displayName}",
            iconName = targetApp.name,
            isEnabled = true,
            triggerType = TriggerType.MANUAL,
            triggerConfig = TriggerConfig(time = "09:00", daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI")),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction(
                    id = UUID.randomUUID().toString(),
                    actionType = "OPEN_APP",
                    targetApp = targetApp,
                    parameters = mapOf("message" to "Automation triggered from OMNIA MAX")
                )
            ),
            targetApp = targetApp,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _selectedWorkflow.value = newWf
    }

    fun saveSelectedWorkflow(workflow: AutomationWorkflow) {
        viewModelScope.launch {
            repository.saveAutomation(workflow)
            _selectedWorkflow.value = null
        }
    }

    fun installTemplate(template: AutomationTemplate) {
        viewModelScope.launch {
            val installedWf = template.workflow.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveAutomation(installedWf)
        }
    }

    fun clearSummary() {
        _lastExecutionSummary.value = null
    }
}
