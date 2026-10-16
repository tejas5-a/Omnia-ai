package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String,
    val providerType: String,
    val systemInstruction: String,
    val isPinned: Boolean,
    val isHighThinking: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val sender: String,
    val agentName: String?,
    val content: String,
    val thinkingContent: String?,
    val timestamp: Long,
    val attachmentsJson: String,
    val status: String,
    val errorMessage: String?
)

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val apiKey: String,
    val baseUrl: String,
    val selectedModel: String,
    val enabled: Boolean,
    val fetchedModelsJson: String,
    val lastTestedMs: Long,
    val connectionStatus: String
)

@Entity(tableName = "agent_task_runs")
data class AgentTaskRunEntity(
    @PrimaryKey val id: String,
    val userPrompt: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val stepsJson: String,
    val finalSummary: String,
    val qualityScore: Int
)

@Entity(tableName = "project_memories")
data class ProjectMemoryEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val key: String,
    val value: String,
    val category: String,
    val timestamp: Long,
    val isEnabled: Boolean
)

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val nodesJson: String,
    val createdAt: Long
)

@Entity(tableName = "workflow_run_logs")
data class WorkflowRunLogEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val workflowName: String,
    val status: String,
    val nodesStateJson: String,
    val outputSummary: String,
    val timestamp: Long
)

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val isEnabled: Boolean,
    val triggerType: String,
    val triggerConfigJson: String,
    val conditionsJson: String,
    val actionsJson: String,
    val targetApp: String,
    val lastRunMs: Long,
    val runCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "automation_run_logs")
data class AutomationRunLogEntity(
    @PrimaryKey val id: String,
    val automationId: String,
    val automationName: String,
    val status: String,
    val stepResultsJson: String,
    val outputSummary: String,
    val durationMs: Long,
    val timestamp: Long
)


@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val fileType: String,
    val fileUri: String,
    val extractedContent: String,
    val tokenCount: Int,
    val timestamp: Long
)

class RoomConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromAttachmentList(list: List<Attachment>): String {
        val type = Types.newParameterizedType(List::class.java, Attachment::class.java)
        return moshi.adapter<List<Attachment>>(type).toJson(list)
    }

    @TypeConverter
    fun toAttachmentList(json: String): List<Attachment> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, Attachment::class.java)
            moshi.adapter<List<Attachment>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromAIModelList(list: List<AIModel>): String {
        val type = Types.newParameterizedType(List::class.java, AIModel::class.java)
        return moshi.adapter<List<AIModel>>(type).toJson(list)
    }

    @TypeConverter
    fun toAIModelList(json: String): List<AIModel> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, AIModel::class.java)
            moshi.adapter<List<AIModel>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromAgentStepLogList(list: List<AgentStepLog>): String {
        val type = Types.newParameterizedType(List::class.java, AgentStepLog::class.java)
        return moshi.adapter<List<AgentStepLog>>(type).toJson(list)
    }

    @TypeConverter
    fun toAgentStepLogList(json: String): List<AgentStepLog> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, AgentStepLog::class.java)
            moshi.adapter<List<AgentStepLog>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromWorkflowNodeList(list: List<WorkflowNode>): String {
        val type = Types.newParameterizedType(List::class.java, WorkflowNode::class.java)
        return moshi.adapter<List<WorkflowNode>>(type).toJson(list)
    }

    @TypeConverter
    fun toWorkflowNodeList(json: String): List<WorkflowNode> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, WorkflowNode::class.java)
            moshi.adapter<List<WorkflowNode>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
}
