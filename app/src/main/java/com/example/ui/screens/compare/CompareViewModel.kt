package com.example.ui.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ComparisonResult
import com.example.data.model.ProviderConfig
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import com.example.engine.ModelComparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val modelA: String = "gemini-3.6-flash",
    val modelB: String = "gpt-4o",
    val promptInput: String = "",
    val comparisonResult: ComparisonResult? = null,
    val isComparing: Boolean = false
)

class CompareViewModel(private val providerRepository: ProviderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            providerRepository.allProviders.collect { list ->
                _uiState.update { it.copy(providers = list) }
            }
        }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(promptInput = prompt) }
    }

    fun setModelA(model: String) {
        _uiState.update { it.copy(modelA = model) }
    }

    fun setModelB(model: String) {
        _uiState.update { it.copy(modelB = model) }
    }

    fun runComparison() {
        val prompt = _uiState.value.promptInput
        if (prompt.isBlank()) return

        _uiState.update { it.copy(isComparing = true) }

        viewModelScope.launch {
            val pA = providerRepository.getActiveProvider()
            val pB = pA // Default provider or secondary
            val result = ModelComparator.compareModels(
                prompt = prompt,
                providerA = pA,
                modelA = _uiState.value.modelA,
                providerB = pB,
                modelB = _uiState.value.modelB,
                providerManager = llmManager
            )
            _uiState.update { it.copy(comparisonResult = result, isComparing = false) }
        }
    }
}
