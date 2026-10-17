package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppPreferencesRepository
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
    val selectedModelId: String = "gemini-2.5-flash",
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
    private var messageObserverJob: Job? = null
    private val llmManager = LLMProviderManager { providerRepository.getActiveProvider() }

    init {
        viewModelScope.launch {
            // 1. Ensure providers are initialized before first query
            providerRepository.initializeDefaultProviders()

            // 2. Sync active provider and user preferences
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

                    val effectiveModel = prefs.selectedModelId.ifBlank { active.selectedModel }

                    _uiState.update {
                        it.copy(
                            activeProvider = active,
                            availableModels = filteredModels,
                            selectedModelId = effectiveModel,
                            isHighThinking = prefs.isHighThinking
                        )
                    }
                }
            }
        }

        // 3. Sync chat threads and active thread reliably
        viewModelScope.launch {
            chatRepository.allThreads.collect { threads ->
                if (threads.isEmpty()) {
                    val defaultModel = appPrefs.userPreferences.value.selectedModelId.ifBlank { "gemini-2.5-flash" }
                    val newT = chatRepository.createNewThread(modelId = defaultModel)
                    appPrefs.updateLastActiveThreadId(newT.id)
                    selectThread(newT.id)
                } else {
                    val currentActiveId = _uiState.value.activeThread?.id
                    val savedActiveId = appPrefs.userPreferences.value.lastActiveThreadId

                    val targetId = when {
                        currentActiveId != null && threads.any { it.id == currentActiveId } -> currentActiveId
                        savedActiveId.isNotBlank() && threads.any { it.id == savedActiveId } -> savedActiveId
                        else -> threads.first().id
                    }

                    val activeThread = threads.firstOrNull { it.id == targetId } ?: threads.first()

                    if (savedActiveId != activeThread.id) {
                        appPrefs.updateLastActiveThreadId(activeThread.id)
                    }

                    _uiState.update {
                        it.copy(
                            threads = threads,
                            activeThread = activeThread,
                            selectedModelId = activeThread.modelId.ifBlank { appPrefs.userPreferences.value.selectedModelId },
                            isHighThinking = activeThread.isHighThinking
                        )
                    }
                    observeMessagesForThread(activeThread.id)
                }
            }
        }
    }

    private fun observeMessagesForThread(threadId: String) {
        messageObserverJob?.cancel()
        messageObserverJob = viewModelScope.launch {
            chatRepository.getMessagesForThread(threadId).collect { msgs ->
                // Ensure messages belong to currently active thread and not during active streaming override
                if (_uiState.value.activeThread?.id == threadId && !_uiState.value.isGenerating) {
                    _uiState.update { it.copy(messages = msgs) }
                }
            }
        }
    }

    fun selectThread(threadId: String) {
        if (threadId.isBlank()) return
        viewModelScope.launch {
            val thread = chatRepository.getThreadById(threadId)
            if (thread != null) {
                appPrefs.updateLastActiveThreadId(thread.id)
                _uiState.update {
                    it.copy(
                        activeThread = thread,
                        selectedModelId = thread.modelId.ifBlank { appPrefs.userPreferences.value.selectedModelId },
                        isHighThinking = thread.isHighThinking
                    )
                }
                if (thread.modelId.isNotBlank()) {
                    appPrefs.updateSelectedModelId(thread.modelId)
                }
                observeMessagesForThread(thread.id)
            }
        }
    }

    fun createNewThread() {
        viewModelScope.launch {
            val currentThread = _uiState.value.activeThread
            val currentMsgs = _uiState.value.messages
            if (currentThread != null && currentThread.title == "New Conversation" && currentMsgs.isEmpty()) {
                appPrefs.updateLastActiveThreadId(currentThread.id)
                return@launch
            }

            val provider = providerRepository.getActiveProvider()
            val currentModel = appPrefs.userPreferences.value.selectedModelId.ifBlank { provider.selectedModel }
            val newThread = chatRepository.createNewThread(
                modelId = currentModel,
                providerType = provider.type
            )
            appPrefs.updateLastActiveThreadId(newThread.id)
            _uiState.update {
                it.copy(
                    activeThread = newThread,
                    messages = emptyList(),
                    selectedModelId = newThread.modelId.ifBlank { appPrefs.userPreferences.value.selectedModelId },
                    isHighThinking = newThread.isHighThinking
                )
            }
            observeMessagesForThread(newThread.id)
        }
    }

    fun sendMessage(promptText: String, attachments: List<Attachment> = emptyList()) {
        if (promptText.isBlank() && attachments.isEmpty()) return

        viewModelScope.launch {
            var activeThread = _uiState.value.activeThread
            if (activeThread == null) {
                val currentModel = appPrefs.userPreferences.value.selectedModelId.ifBlank { "gemini-2.5-flash" }
                activeThread = chatRepository.createNewThread(modelId = currentModel)
                appPrefs.updateLastActiveThreadId(activeThread.id)
                _uiState.update { it.copy(activeThread = activeThread) }
            }

            val userMsg = ChatMessage(
                threadId = activeThread.id,
                sender = MessageSender.USER,
                content = promptText.trim(),
                attachments = attachments,
                status = MessageStatus.SUCCESS
            )
            chatRepository.saveMessage(userMsg)

            if (_uiState.value.messages.isEmpty() || activeThread.title == "New Conversation") {
                val title = if (promptText.length > 30) promptText.take(28) + "..." else promptText
                chatRepository.renameThread(activeThread.id, title)
                val updatedThread = activeThread.copy(title = title)
                _uiState.update { it.copy(activeThread = updatedThread) }
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

            // Save initial assistant placeholder in DB and memory
            chatRepository.saveMessage(assistantMsg)
            val currentList = _uiState.value.messages.filter { it.id != assistantMsgId }.toMutableList()
            currentList.add(assistantMsg)
            _uiState.update { it.copy(messages = currentList) }

            val historyForModel = currentList.filter { it.id != assistantMsgId && it.status == MessageStatus.SUCCESS }
            val fullAccumulatedText = StringBuilder()
            var lastSaveTime = System.currentTimeMillis()

            try {
                val effectiveModel = if (thread.modelId.isNotBlank()) {
                    thread.modelId
                } else {
                    appPrefs.userPreferences.value.selectedModelId.ifBlank { "gemini-2.5-flash" }
                }

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

                    // Update memory state
                    val updatedList = _uiState.value.messages.map { msg ->
                        if (msg.id == assistantMsgId) assistantMsg else msg
                    }
                    _uiState.update { it.copy(messages = updatedList) }

                    // Throttle DB saves during stream
                    val now = System.currentTimeMillis()
                    if (now - lastSaveTime > 300) {
                        lastSaveTime = now
                        chatRepository.saveMessage(assistantMsg)
                    }
                }

                val finalOutput = fullAccumulatedText.toString().trim()
                if (finalOutput.isEmpty()) {
                    assistantMsg = assistantMsg.copy(
                        status = MessageStatus.ERROR,
                        errorMessage = "Model returned an empty response. Please verify your API key and model selection."
                    )
                    _uiState.update { it.copy(errorText = assistantMsg.errorMessage) }
                } else {
                    assistantMsg = assistantMsg.copy(
                        content = finalOutput,
                        status = MessageStatus.SUCCESS
                    )
                }

                // Final commit to Room DB before setting isGenerating to false
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
            if (_uiState.value.activeThread?.id == threadId) {
                val remaining = _uiState.value.threads.filter { it.id != threadId }
                val next = remaining.firstOrNull()
                if (next != null) {
                    selectThread(next.id)
                } else {
                    createNewThread()
                }
            }
        }
    }

    fun renameThread(threadId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameThread(threadId, newTitle)
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
