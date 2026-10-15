package com.example.data.model

data class Project(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProjectMemory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val key: String,
    val value: String,
    val category: String = "DECISION", // DECISION, CONTEXT, ARCHITECTURE, USER_PREFERENCE
    val timestamp: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true
)
