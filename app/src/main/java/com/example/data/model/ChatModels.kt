package com.example.data.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM,
    AGENT
}

enum class MessageStatus {
    SENDING,
    STREAMING,
    SUCCESS,
    ERROR
}

data class Attachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String = "IMAGE", // IMAGE or DOC
    val uri: String = "",
    val fileName: String = "",
    val mimeType: String = "image/jpeg",
    val base64Data: String? = null
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val threadId: String,
    val sender: MessageSender,
    val agentName: String? = null,
    val content: String,
    val thinkingContent: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList(),
    val status: MessageStatus = MessageStatus.SUCCESS,
    val errorMessage: String? = null
)

data class ChatThread(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelId: String = "gemini-3.6-flash",
    val providerType: ProviderType = ProviderType.GEMINI,
    val systemInstruction: String = "You are OMNIA MAX, an elite AI assistant. Provide concise, expert, helpful, and insightful responses.",
    val isPinned: Boolean = false,
    val isHighThinking: Boolean = false
)
