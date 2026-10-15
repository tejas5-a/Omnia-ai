package com.example.ui.screens.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ProjectMemory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryUiState(
    val memories: List<ProjectMemory> = emptyList()
)

class MemoryViewModel(private val memoryRepository: MemoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            memoryRepository.allMemories.collect { list ->
                if (list.isEmpty()) {
                    val defaultMemory = ProjectMemory(
                        projectId = "default",
                        key = "Architecture Style",
                        value = "Always write clean Kotlin Jetpack Compose code with MVVM and Room DB."
                    )
                    memoryRepository.saveMemory(defaultMemory)
                }
                _uiState.update { it.copy(memories = list) }
            }
        }
    }

    fun addMemory(key: String, value: String) {
        viewModelScope.launch {
            val mem = ProjectMemory(projectId = "default", key = key, value = value)
            memoryRepository.saveMemory(mem)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }
}
