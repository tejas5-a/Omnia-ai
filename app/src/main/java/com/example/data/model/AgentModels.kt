package com.example.data.model

enum class AgentType(
    val title: String,
    val description: String,
    val systemPrompt: String
) {
    RESEARCH(
        title = "Research Agent",
        description = "Deconstructs complex tasks, gathers technical context, and formulates strategic blueprints.",
        systemPrompt = "You are the Research Agent in OMNIA MAX. Analyze the user request deeply, break down key concepts, list required knowledge, and structure an actionable blueprint."
    ),
    CODING(
        title = "Coding Agent",
        description = "Engineers clean, performant, type-safe code modules, architectures, and algorithms.",
        systemPrompt = "You are the Coding Agent in OMNIA MAX. Implement production-grade code, clean design patterns, and robust logic matching the research specifications."
    ),
    TESTING(
        title = "Testing Agent",
        description = "Formulates comprehensive unit test suites, edge case validations, and test scenarios.",
        systemPrompt = "You are the Testing Agent in OMNIA MAX. Analyze code and architecture to write exhaustive unit tests, edge case assertions, and boundary validation scripts."
    ),
    WRITING(
        title = "Writing Agent",
        description = "Drafts technical documentation, developer guides, user release notes, and executive summaries.",
        systemPrompt = "You are the Writing Agent in OMNIA MAX. Synthesize outputs into crisp, professional technical documentation, user guides, and clear summaries."
    ),
    SECURITY(
        title = "Security Agent",
        description = "Performs security audits, key leak checks, input sanitization checks, and threat modeling.",
        systemPrompt = "You are the Security Agent in OMNIA MAX. Audit code and plans for vulnerabilities, injection risks, hardcoded secrets, and compliance flaws."
    ),
    REVIEW(
        title = "Review Agent",
        description = "Synthesizes all agent outputs, conducts final quality review, and provides verdict & summary.",
        systemPrompt = "You are the Review Agent in OMNIA MAX. Perform final verification across all agent outputs, highlight remaining risks, provide a quality score (1-100), and present the aggregated solution."
    )
}

enum class AgentRunStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED
}

enum class StepStatus {
    PENDING,
    THINKING,
    EXECUTING,
    COMPLETED,
    FAILED
}

data class AgentStepLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val taskId: String,
    val agentType: AgentType,
    val status: StepStatus = StepStatus.PENDING,
    val inputPrompt: String = "",
    val outputResult: String = "",
    val thoughtLog: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0
)

data class AgentTaskRun(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userPrompt: String,
    val status: AgentRunStatus = AgentRunStatus.RUNNING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val steps: List<AgentStepLog> = emptyList(),
    val finalSummary: String = "",
    val qualityScore: Int = 0
)
