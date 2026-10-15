package com.example.ui.screens.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.model.DocumentItem
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KnowledgeUiState(
    val documents: List<DocumentItem> = emptyList(),
    val searchQuery: String = "",
    val questionInput: String = "",
    val qaAnswer: String? = null,
    val isSearching: Boolean = false
)

class KnowledgeViewModel(
    private val documentDao: DocumentDao,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeUiState())
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            documentDao.getAllDocuments().collect { list ->
                val docs = list.map {
                    DocumentItem(
                        id = it.id,
                        fileName = it.fileName,
                        fileType = it.fileType,
                        fileUri = it.fileUri,
                        extractedContent = it.extractedContent,
                        tokenCount = it.tokenCount,
                        timestamp = it.timestamp
                    )
                }
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }

    fun addTextDocument(fileName: String, content: String) {
        viewModelScope.launch {
            val entity = DocumentEntity(
                id = java.util.UUID.randomUUID().toString(),
                fileName = fileName,
                fileType = "TXT",
                fileUri = "",
                extractedContent = content,
                tokenCount = content.length / 4,
                timestamp = System.currentTimeMillis()
            )
            documentDao.insertDocument(entity)
        }
    }

    fun updateQuestion(q: String) {
        _uiState.update { it.copy(questionInput = q) }
    }

    fun askQuestionOverKnowledge() {
        val q = _uiState.value.questionInput
        val docs = _uiState.value.documents
        if (q.isBlank() || docs.isEmpty()) return

        _uiState.update { it.copy(isSearching = true) }

        viewModelScope.launch {
            val contextText = docs.joinToString("\n---\n") { "${it.fileName}:\n${it.extractedContent}" }
            val prompt = "Based on the following knowledge documents, answer the user's question with citations:\n\nDOCUMENTS:\n$contextText\n\nQUESTION: $q"

            val answer = try {
                llmManager.singleTurnQuery(
                    systemInstruction = "You are a Knowledge Assistant analyzing uploaded documents.",
                    prompt = prompt
                )
            } catch (e: Exception) {
                "Answer based on document context."
            }

            _uiState.update { it.copy(qaAnswer = answer, isSearching = false, questionInput = "") }
        }
    }
}
