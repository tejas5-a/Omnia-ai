package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :id LIMIT 1")
    suspend fun getThreadById(id: String): ChatThreadEntity?

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getMessagesForThreadSync(threadId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThreadEntity)

    @Update
    suspend fun updateThread(thread: ChatThreadEntity)

    @Query("DELETE FROM chat_threads WHERE id = :id")
    suspend fun deleteThread(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun clearMessagesForThread(threadId: String)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM provider_configs ORDER BY id ASC")
    fun getAllProviders(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs ORDER BY id ASC")
    suspend fun getProvidersSync(): List<ProviderConfigEntity>

    @Query("SELECT * FROM provider_configs WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderConfigEntity)

    @Update
    suspend fun updateProvider(provider: ProviderConfigEntity)

    @Query("DELETE FROM provider_configs WHERE id = :id")
    suspend fun deleteProvider(id: String)
}

@Dao
interface AgentTaskDao {
    @Query("SELECT * FROM agent_task_runs ORDER BY createdAt DESC")
    fun getAllTaskRuns(): Flow<List<AgentTaskRunEntity>>

    @Query("SELECT * FROM agent_task_runs WHERE id = :id LIMIT 1")
    suspend fun getTaskRunById(id: String): AgentTaskRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskRun(taskRun: AgentTaskRunEntity)

    @Update
    suspend fun updateTaskRun(taskRun: AgentTaskRunEntity)

    @Query("DELETE FROM agent_task_runs WHERE id = :id")
    suspend fun deleteTaskRun(id: String)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM project_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<ProjectMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: ProjectMemoryEntity)

    @Query("DELETE FROM project_memories WHERE id = :id")
    suspend fun deleteMemory(id: String)
}

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY createdAt DESC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun deleteWorkflow(id: String)

    @Query("SELECT * FROM workflow_run_logs ORDER BY timestamp DESC")
    fun getAllRunLogs(): Flow<List<WorkflowRunLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunLog(log: WorkflowRunLogEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)
}
