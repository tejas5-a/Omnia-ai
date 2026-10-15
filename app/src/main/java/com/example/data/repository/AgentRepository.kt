package com.example.data.repository

import com.example.data.local.AgentTaskDao
import com.example.data.local.AgentTaskRunEntity
import com.example.data.local.RoomConverters
import com.example.data.model.AgentRunStatus
import com.example.data.model.AgentTaskRun
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AgentRepository(private val agentTaskDao: AgentTaskDao) {

    val allTaskRuns: Flow<List<AgentTaskRun>> = agentTaskDao.getAllTaskRuns().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getTaskRunById(id: String): AgentTaskRun? {
        return agentTaskDao.getTaskRunById(id)?.toDomain()
    }

    suspend fun saveTaskRun(run: AgentTaskRun) {
        agentTaskDao.insertTaskRun(run.toEntity())
    }

    suspend fun deleteTaskRun(id: String) {
        agentTaskDao.deleteTaskRun(id)
    }

    private fun AgentTaskRunEntity.toDomain(): AgentTaskRun {
        val statusEnum = try { AgentRunStatus.valueOf(status) } catch (e: Exception) { AgentRunStatus.COMPLETED }
        val converter = RoomConverters()
        val steps = converter.toAgentStepLogList(stepsJson)

        return AgentTaskRun(
            id = id,
            userPrompt = userPrompt,
            status = statusEnum,
            createdAt = createdAt,
            updatedAt = updatedAt,
            steps = steps,
            finalSummary = finalSummary,
            qualityScore = qualityScore
        )
    }

    private fun AgentTaskRun.toEntity(): AgentTaskRunEntity {
        val converter = RoomConverters()
        return AgentTaskRunEntity(
            id = id,
            userPrompt = userPrompt,
            status = status.name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            stepsJson = converter.fromAgentStepLogList(steps),
            finalSummary = finalSummary,
            qualityScore = qualityScore
        )
    }
}
