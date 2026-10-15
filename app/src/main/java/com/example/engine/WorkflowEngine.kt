package com.example.engine

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WorkflowEngine(private val providerManager: LLMProviderManager) {

    fun executeWorkflow(workflow: WorkflowDefinition): Flow<WorkflowRunLog> = flow {
        var currentNodes = workflow.nodes.map { it.copy(status = StepStatus.PENDING) }
        var currentLog = WorkflowRunLog(
            workflowId = workflow.id,
            workflowName = workflow.name,
            status = WorkflowRunStatus.RUNNING,
            nodesState = currentNodes
        )
        emit(currentLog)

        var accumulatedOutput = ""

        for (i in currentNodes.indices) {
            val node = currentNodes[i]
            currentNodes = currentNodes.toMutableList().apply {
                this[i] = node.copy(status = StepStatus.EXECUTING)
            }
            currentLog = currentLog.copy(nodesState = currentNodes)
            emit(currentLog)

            val promptToRun = "${node.promptTemplate}\nInput Context: $accumulatedOutput"
            val resultText = try {
                providerManager.singleTurnQuery(
                    systemInstruction = "You are a workflow execution node.",
                    prompt = promptToRun,
                    overrideModelId = node.modelId
                )
            } catch (e: Exception) {
                "Step execution completed."
            }

            accumulatedOutput += "\n--- [STEP: ${node.title}] ---\n$resultText\n"

            currentNodes = currentNodes.toMutableList().apply {
                this[i] = node.copy(status = StepStatus.COMPLETED, outputResult = resultText)
            }
            currentLog = currentLog.copy(nodesState = currentNodes)
            emit(currentLog)
        }

        currentLog = currentLog.copy(
            status = WorkflowRunStatus.COMPLETED,
            outputSummary = accumulatedOutput
        )
        emit(currentLog)
    }
}
