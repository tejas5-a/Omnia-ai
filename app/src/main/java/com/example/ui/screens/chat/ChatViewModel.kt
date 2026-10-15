package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppPreferencesRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ProviderRepository
import com.example.engine.LLMProviderManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val providerRepository: ProviderRepository,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            providerRepository.initializeDefaultProviders()

            combine(providerRepository.allProviders, appPrefs.userPreferences) { providers, prefs ->
                val active = providers.firstOrNull { it.id == prefs.activeProviderId }
                    ?: providers.firstOrNull { it.enabled }
                    ?: providers.firstOrNull()

                Pair(active, prefs)
            }.collect { (active, prefs) ->
                if (active != null) {
                    val filteredModels = if (prefs.isFreeModelsOnly) {
                        active.fetchedModels.filter { it.isFreeTier }
                            .ifEmpty { active.fetchedModels }
                    } else {
                        active.fetchedModels
                    }

                    _uiState.update {
                        it.copy(
                            activeProvider = active,
                            availableModels = filteredModels,
                            selectedModelId = prefs.selectedModelId,
                            isHighThinking = prefs.isHighThinking
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            chatRepository.allThreads.collect { threads ->
                if (threads.isEmpty()) {
                    val newT = chatRepository.createNewThread(
                        modelId = appPrefs.userPreferences.value.selectedModelId
                    )
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
                // Don't overwrite active in-memory streaming message
                if (!_uiState.value.isGenerating) {
                    _uiState.update { it.copy(messages = msgs) }
                }
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
                appPrefs.updateSelectedModelId(thread.modelId)
                observeMessagesForThread(threadId)
            }
        }
    }

    fun createNewThread() {
        viewModelScope.launch {
            val provider = providerRepository.getActiveProvider()
            val currentModel = appPrefs.userPreferences.value.selectedModelId
            val newThread = chatRepository.createNewThread(
                modelId = currentModel,
                providerType = provider.type
            )
            selectThread(newThread.id)
        }
    }

    fun sendMessage(promptText: String, attachments: List<Attachment> = emptyList()) {
        if (promptText.isBlank() && attachments.isEmpty()) return

        viewModelScope.launch {
            // Ensure thread exists
            var activeThread = _uiState.value.activeThread
            if (activeThread == null) {
                activeThread = chatRepository.createNewThread(
                    modelId = appPrefs.userPreferences.value.selectedModelId
                )
                _uiState.update { it.copy(activeThread = activeThread) }
            }

            val userMsg = ChatMessage(
                threadId = activeThread.id,
                sender = MessageSender.USER,
                content = promptText,
                attachments = attachments,
                status = MessageStatus.SUCCESS
            )
            chatRepository.saveMessage(userMsg)

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

            // Append assistant message in UI state immediately
            val currentList = _uiState.value.messages.toMutableList()
            currentList.add(assistantMsg)
            _uiState.update { it.copy(messages = currentList) }

            val historyForModel = currentList.filter { it.id != assistantMsgId && it.status == MessageStatus.SUCCESS }
            val fullAccumulatedText = StringBuilder()
            var lastSaveTime = System.currentTimeMillis()

            try {
                val effectiveModel = if (thread.modelId.isNotBlank()) thread.modelId else appPrefs.userPreferences.value.selectedModelId

                llmManager.streamResponse(
                    messages = historyForModel,
                    systemInstruction = thread.systemInstruction,
                    overrideModelId = effectiveModel,
                    isHighThinking = thread.isHighThinking
                ).collect { chunk ->
                    fullAccumulatedText.append(chunk)
                    assistantMsg = assistantMsg.copy(
                        content = fullAccumulatedText.toString(),
                        status = MessageStatus.STREAMING
                    )

                    // Update UI State in memory
                    val updatedList = _uiState.value.messages.map { msg ->
                        if (msg.id == assistantMsgId) assistantMsg else msg
                    }
                    _uiState.update { it.copy(messages = updatedList) }

                    // Periodically throttle DB save (every 250ms) to prevent Room flow re-creation
                    val now = System.currentTimeMillis()
                    if (now - lastSaveTime > 250) {
                        lastSaveTime = now
                        chatRepository.saveMessage(assistantMsg)
                    }
                }

                val finalOutput = fullAccumulatedText.toString().trim()
                if (finalOutput.isEmpty()) {
                    assistantMsg = assistantMsg.copy(
                        status = MessageStatus.ERROR,
                        errorMessage = "Model returned empty response. Please check model ID or API key."
                    )
                    _uiState.update { it.copy(errorText = assistantMsg.errorMessage) }
                } else {
                    assistantMsg = assistantMsg.copy(
                        content = finalOutput,
                        status = MessageStatus.SUCCESS
                    )
                }

                // Final commit to Room DB
                chatRepository.saveMessage(assistantMsg)

                val finalList = _uiState.value.messages.map { msg ->
                    if (msg.id == assistantMsgId) assistantMsg else msg
                }
                _uiState.update { it.copy(messages = finalList) }

            } catch (e: Exception) {
                assistantMsg = assistantMsg.copy(
                    status = MessageStatus.ERROR,
                    errorMessage = e.localizedMessage ?: "Generation failed"
                )
                chatRepository.saveMessage(assistantMsg)

                val finalList = _uiState.value.messages.map { msg ->
                    if (msg.id == assistantMsgId) assistantMsg else msg
                }
                _uiState.update { it.copy(messages = finalList, errorText = e.localizedMessage) }
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
            appPrefs.updateSelectedModelId(modelId)
            _uiState.update { it.copy(activeThread = updated, selectedModelId = modelId) }
        }
    }

    fun toggleThinkingMode(enabled: Boolean) {
        val activeThread = _uiState.value.activeThread ?: return
        viewModelScope.launch {
            val updated = activeThread.copy(isHighThinking = enabled)
            chatRepository.saveThread(updated)
            appPrefs.updateHighThinking(enabled)
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
