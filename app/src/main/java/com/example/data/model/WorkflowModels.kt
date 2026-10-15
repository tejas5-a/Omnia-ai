package com.example.data.model

enum class StepType {
    AGENT,
    PROMPT,
    TOOL,
    CONDITION
}

enum class WorkflowRunStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class WorkflowNode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val stepType: StepType,
    val agentType: AgentType? = null,
    val modelId: String? = null,
    val promptTemplate: String = "",
    val outputResult: String = "",
    val status: StepStatus = StepStatus.PENDING
)

data class WorkflowDefinition(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val nodes: List<WorkflowNode> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkflowRunLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val workflowId: String,
    val workflowName: String,
    val status: WorkflowRunStatus = WorkflowRunStatus.IDLE,
    val nodesState: List<WorkflowNode> = emptyList(),
    val outputSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
