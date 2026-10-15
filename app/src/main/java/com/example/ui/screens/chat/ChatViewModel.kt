package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ChatRepository
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val threads: List<ChatThread> = emptyList(),
    val activeThread: ChatThread? = null,
    val messages: List<ChatMessage> = emptyList(),
    val activeProvider: ProviderConfig? = null,
    val availableModels: List<AIModel> = emptyList(),
    val selectedModelId: String = "gemini-3.6-flash",
    val isGenerating: Boolean = false,
    val isHighThinking: Boolean = false,
    val systemPromptSheetOpen: Boolean = false,
    val errorText: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            providerRepository.initializeDefaultProviders()

            // Observe providers
            providerRepository.allProviders.collect { providers ->
                val active = providers.firstOrNull { it.enabled } ?: providers.firstOrNull()
                if (active != null) {
                    _uiState.update {
                        it.copy(
                            activeProvider = active,
                            availableModels = active.fetchedModels,
                            selectedModelId = active.selectedModel
                        )
                    }
                }
            }
        }

        // Observe chat threads
        viewModelScope.launch {
            chatRepository.allThreads.collect { threads ->
                if (threads.isEmpty()) {
                    val newT = chatRepository.createNewThread()
                    selectThread(newT.id)
                } else {
                    val currentActive = _uiState.value.activeThread
                    val activeId = currentActive?.id ?: threads.first().id
                    val activeThread = threads.firstOrNull { it.id == activeId } ?: threads.first()
                    _uiState.update {
                        it.copy(
                            threads = threads,
                            activeThread = activeThread,
                            selectedModelId = activeThread.modelId,
                            isHighThinking = activeThread.isHighThinking
                        )
                    }
                    observeMessagesForThread(activeThread.id)
                }
            }
        }
    }

    private fun observeMessagesForThread(threadId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesForThread(threadId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun selectThread(threadId: String) {
        viewModelScope.launch {
            val thread = chatRepository.getThreadById(threadId)
            if (thread != null) {
                _uiState.update {
                    it.copy(
                        activeThread = thread,
                        selectedModelId = thread.modelId,
                        isHighThinking = thread.isHighThinking
                    )
                }
                observeMessagesForThread(threadId)
            }
        }
    }

    fun createNewThread() {
        viewModelScope.launch {
            val provider = providerRepository.getActiveProvider()
            val newThread = chatRepository.createNewThread(
                modelId = provider.selectedModel,
                providerType = provider.type
            )
            selectThread(newThread.id)
        }
    }

    fun sendMessage(promptText: String, attachments: List<Attachment> = emptyList()) {
        if (promptText.isBlank() && attachments.isEmpty()) return

        val activeThread = _uiState.value.activeThread ?: return

        viewModelScope.launch {
            val userMsg = ChatMessage(
                threadId = activeThread.id,
                sender = MessageSender.USER,
                content = promptText,
                attachments = attachments,
                status = MessageStatus.SUCCESS
            )
            chatRepository.saveMessage(userMsg)

            // Update title if it's the first user message
            if (_uiState.value.messages.isEmpty() || activeThread.title == "New Conversation") {
                val title = if (promptText.length > 30) promptText.take(28) + "..." else promptText
                chatRepository.renameThread(activeThread.id, title)
            }

            triggerAiResponse(activeThread)
        }
    }

    private fun triggerAiResponse(thread: ChatThread) {
        generationJob?.cancel()
        _uiState.update { it.copy(isGenerating = true, errorText = null) }

        generationJob = viewModelScope.launch {
            val assistantMsgId = java.util.UUID.randomUUID().toString()
            var assistantMsg = ChatMessage(
                id = assistantMsgId,
                threadId = thread.id,
                sender = MessageSender.ASSISTANT,
                content = "",
                status = MessageStatus.STREAMING
            )
            chatRepository.saveMessage(assistantMsg)

            val currentMsgs = chatRepository.getMessagesForThread(thread.id).first()
                .filter { it.id != assistantMsgId }

            val fullAccumulatedText = StringBuilder()

            try {
                llmManager.streamResponse(
                    messages = currentMsgs,
                    systemInstruction = thread.systemInstruction,
                    overrideModelId = thread.modelId,
                    isHighThinking = thread.isHighThinking
                ).collect { chunk ->
                    fullAccumulatedText.append(chunk)
                    assistantMsg = assistantMsg.copy(
                        content = fullAccumulatedText.toString(),
                        status = MessageStatus.STREAMING
                    )
                    chatRepository.saveMessage(assistantMsg)
                }

                assistantMsg = assistantMsg.copy(
                    content = fullAccumulatedText.toString(),
                    status = MessageStatus.SUCCESS
                )
                chatRepository.saveMessage(assistantMsg)
            } catch (e: Exception) {
                assistantMsg = assistantMsg.copy(
                    status = MessageStatus.ERROR,
                    errorMessage = e.localizedMessage ?: "Generation failed"
                )
                chatRepository.saveMessage(assistantMsg)
                _uiState.update { it.copy(errorText = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun regenerateResponse() {
        val activeThread = _uiState.value.activeThread ?: return
        triggerAiResponse(activeThread)
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            chatRepository.deleteThread(threadId)
        }
    }

    fun setSelectedModel(modelId: String) {
        val activeThread = _uiState.value.activeThread ?: return
        viewModelScope.launch {
            val updated = activeThread.copy(modelId = modelId)
            chatRepository.saveThread(updated)
            _uiState.update { it.copy(activeThread = updated, selectedModelId = modelId) }
        }
    }

    fun toggleThinkingMode(enabled: Boolean) {
        val activeThread = _uiState.value.activeThread ?: return
        viewModelScope.launch {
            val updated = activeThread.copy(isHighThinking = enabled)
            chatRepository.saveThread(updated)
            _uiState.update { it.copy(activeThread = updated, isHighThinking = enabled) }
        }
    }

    fun updateSystemInstruction(newInstruction: String) {
        val activeThread = _uiState.value.activeThread ?: return
        viewModelScope.launch {
            val updated = activeThread.copy(systemInstruction = newInstruction)
            chatRepository.saveThread(updated)
            _uiState.update { it.copy(activeThread = updated, systemPromptSheetOpen = false) }
        }
    }

    fun setSystemPromptSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(systemPromptSheetOpen = isOpen) }
    }
}
