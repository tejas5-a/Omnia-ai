package com.example.data.model

data class DocumentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileType: String = "TXT", // TXT, PDF, MD, CODE
    val fileUri: String = "",
    val extractedContent: String = "",
    val tokenCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CodeFileItem(
    val path: String,
    val name: String,
    val content: String,
    val isDirectory: Boolean = false,
    val language: String = "kotlin"
)
