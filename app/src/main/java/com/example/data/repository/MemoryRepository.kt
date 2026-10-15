package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.local.ProjectMemoryEntity
import com.example.data.model.ProjectMemory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(private val memoryDao: MemoryDao) {

    val allMemories: Flow<List<ProjectMemory>> = memoryDao.getAllMemories().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveMemory(memory: ProjectMemory) {
        memoryDao.insertMemory(memory.toEntity())
    }

    suspend fun deleteMemory(id: String) {
        memoryDao.deleteMemory(id)
    }

    private fun ProjectMemoryEntity.toDomain(): ProjectMemory {
        return ProjectMemory(
            id = id,
            projectId = projectId,
            key = key,
            value = value,
            category = category,
            timestamp = timestamp,
            isEnabled = isEnabled
        )
    }

    private fun ProjectMemory.toEntity(): ProjectMemoryEntity {
        return ProjectMemoryEntity(
            id = id,
            projectId = projectId,
            key = key,
            value = value,
            category = category,
            timestamp = timestamp,
            isEnabled = isEnabled
        )
    }
}
