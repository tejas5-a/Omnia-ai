package com.example.automation

import android.content.Context
import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AutomationEngine(
    private val context: Context,
    private val repository: AutomationRepository,
    private val integrationManager: CrossAppIntegrationManager = CrossAppIntegrationManager(context)
) {

    suspend fun executeWorkflow(
        workflow: AutomationWorkflow,
        runtimeInputs: Map<String, String> = emptyMap()
    ): AutomationRunLog = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val stepResults = mutableListOf<StepExecutionResult>()
        var overallSuccess = true
        val contextVariables = runtimeInputs.toMutableMap()

        // 1. Evaluate IF Conditions
        var conditionsPassed = true
        for (condition in workflow.conditions) {
            val stepStart = System.currentTimeMillis()
            val eval = evaluateCondition(condition, contextVariables)
            stepResults.add(
                StepExecutionResult(
                    stepId = condition.id,
                    stepName = "IF ${condition.field} ${condition.operator} ${condition.targetValue}",
                    success = eval,
                    output = if (eval) "Condition Passed" else "Condition Failed",
                    durationMs = System.currentTimeMillis() - stepStart
                )
            )
            if (!eval) {
                conditionsPassed = false
                overallSuccess = false
                break
            }
        }

        if (!conditionsPassed) {
            val duration = System.currentTimeMillis() - startTime
            val log = AutomationRunLog(
                id = UUID.randomUUID().toString(),
                automationId = workflow.id,
                automationName = workflow.name,
                status = ExecutionStatus.SKIPPED,
                stepResults = stepResults,
                outputSummary = "Execution stopped: Condition check failed.",
                durationMs = duration,
                timestamp = startTime
            )
            repository.logRun(log)
            return@withContext log
        }

        // 2. Execute THEN Actions
        for (action in workflow.actions) {
            val stepStart = System.currentTimeMillis()
            val targetApp = if (action.targetApp != CrossAppTarget.SYSTEM) action.targetApp else workflow.targetApp

            val result = integrationManager.executeAction(
                target = targetApp,
                action = action,
                variables = contextVariables
            )

            // Merge action output data into variable context
            result.data.forEach { (k, v) -> contextVariables[k] = v }

            stepResults.add(
                StepExecutionResult(
                    stepId = action.id,
                    stepName = "${action.actionType} (${targetApp.displayName})",
                    success = result.success,
                    output = result.outputSummary,
                    durationMs = System.currentTimeMillis() - stepStart
                )
            )

            if (!result.success) {
                overallSuccess = false
                break
            }
        }

        val duration = System.currentTimeMillis() - startTime
        val status = if (overallSuccess) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED
        val summary = if (overallSuccess) {
            "Workflow '${workflow.name}' executed ${workflow.actions.size} actions successfully across ${workflow.targetApp.displayName}."
        } else {
            "Workflow '${workflow.name}' failed on action step."
        }

        val runLog = AutomationRunLog(
            id = UUID.randomUUID().toString(),
            automationId = workflow.id,
            automationName = workflow.name,
            status = status,
            stepResults = stepResults,
            outputSummary = summary,
            durationMs = duration,
            timestamp = startTime
        )

        repository.logRun(runLog)
        return@withContext runLog
    }

    private fun evaluateCondition(condition: AutomationCondition, variables: Map<String, String>): Boolean {
        val fieldValue = variables[condition.field] ?: ""
        val target = condition.targetValue

        return when (condition.operator) {
            "EQUALS" -> fieldValue.equals(target, ignoreCase = true)
            "NOT_EQUALS" -> !fieldValue.equals(target, ignoreCase = true)
            "CONTAINS" -> fieldValue.contains(target, ignoreCase = true)
            "GREATER_THAN" -> (fieldValue.toDoubleOrNull() ?: 0.0) > (target.toDoubleOrNull() ?: 0.0)
            "LESS_THAN" -> (fieldValue.toDoubleOrNull() ?: 0.0) < (target.toDoubleOrNull() ?: 0.0)
            else -> true
        }
    }
}
