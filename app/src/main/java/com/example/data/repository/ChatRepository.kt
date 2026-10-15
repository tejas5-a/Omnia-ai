package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatThreadEntity
import com.example.data.local.RoomConverters
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val chatDao: ChatDao) {

    val allThreads: Flow<List<ChatThread>> = chatDao.getAllThreads().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForThread(threadId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getThreadById(id: String): ChatThread? {
        return chatDao.getThreadById(id)?.toDomain()
    }

    suspend fun createNewThread(
        title: String = "New Conversation",
        modelId: String = "gemini-3.6-flash",
        providerType: ProviderType = ProviderType.GEMINI,
        systemInstruction: String = "You are OMNIA MAX, an elite AI assistant.",
        isHighThinking: Boolean = false
    ): ChatThread {
        val newThread = ChatThread(
            title = title,
            modelId = modelId,
            providerType = providerType,
            systemInstruction = systemInstruction,
            isHighThinking = isHighThinking
        )
        chatDao.insertThread(newThread.toEntity())
        return newThread
    }

    suspend fun saveThread(thread: ChatThread) {
        chatDao.insertThread(thread.toEntity())
    }

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message.toEntity())
        // Touch thread updatedAt
        val thread = chatDao.getThreadById(message.threadId)
        if (thread != null) {
            val updated = thread.copy(updatedAt = System.currentTimeMillis())
            chatDao.updateThread(updated)
        }
    }

    suspend fun updateMessage(message: ChatMessage) {
        chatDao.updateMessage(message.toEntity())
    }

    suspend fun deleteThread(threadId: String) {
        chatDao.clearMessagesForThread(threadId)
        chatDao.deleteThread(threadId)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun renameThread(threadId: String, newTitle: String) {
        val thread = chatDao.getThreadById(threadId)
        if (thread != null) {
            chatDao.updateThread(thread.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun exportChatJson(threadId: String): String {
        val thread = chatDao.getThreadById(threadId) ?: return "{}"
        val messages = chatDao.getMessagesForThreadSync(threadId).map { it.toDomain() }

        val obj = org.json.JSONObject()
        obj.put("id", thread.id)
        obj.put("title", thread.title)
        obj.put("modelId", thread.modelId)
        obj.put("createdAt", thread.createdAt)

        val msgsArr = org.json.JSONArray()
        for (m in messages) {
            val mObj = org.json.JSONObject()
            mObj.put("sender", m.sender.name)
            mObj.put("content", m.content)
            mObj.put("timestamp", m.timestamp)
            msgsArr.put(mObj)
        }
        obj.put("messages", msgsArr)

        return obj.toString(2)
    }

    private fun ChatThreadEntity.toDomain(): ChatThread {
        val providerEnum = try { ProviderType.valueOf(providerType) } catch (e: Exception) { ProviderType.GEMINI }
        return ChatThread(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            modelId = modelId,
            providerType = providerEnum,
            systemInstruction = systemInstruction,
            isPinned = isPinned,
            isHighThinking = isHighThinking
        )
    }

    private fun ChatThread.toEntity(): ChatThreadEntity {
        return ChatThreadEntity(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            modelId = modelId,
            providerType = providerType.name,
            systemInstruction = systemInstruction,
            isPinned = isPinned,
            isHighThinking = isHighThinking
        )
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        val senderEnum = try { MessageSender.valueOf(sender) } catch (e: Exception) { MessageSender.USER }
        val statusEnum = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SUCCESS }
        val converter = RoomConverters()
        val atts = converter.toAttachmentList(attachmentsJson)

        return ChatMessage(
            id = id,
            threadId = threadId,
            sender = senderEnum,
            agentName = agentName,
            content = content,
            thinkingContent = thinkingContent,
            timestamp = timestamp,
            attachments = atts,
            status = statusEnum,
            errorMessage = errorMessage
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        val converter = RoomConverters()
        return ChatMessageEntity(
            id = id,
            threadId = threadId,
            sender = sender.name,
            agentName = agentName,
            content = content,
            thinkingContent = thinkingContent,
            timestamp = timestamp,
            attachmentsJson = converter.fromAttachmentList(attachments),
            status = status.name,
            errorMessage = errorMessage
        )
    }
}
