package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assistant.engine.AssistantCommandParser
import com.example.assistant.engine.DeviceControlExecutor
import com.example.assistant.engine.VoiceAssistantManager
import com.example.assistant.messaging.ContactResolver
import com.example.assistant.messaging.MessagingDispatcher
import com.example.assistant.model.AssistantCommand
import com.example.assistant.model.AssistantState
import com.example.assistant.model.CommandExecutionResult
import com.example.assistant.model.ResolvedContact
import com.example.assistant.ui.AssistantSettingsScreen
import com.example.assistant.ui.AssistantVoiceOverlay
import com.example.data.local.AppDatabase
import com.example.data.local.SecurePreferencesManager
import com.example.data.repository.*
import com.example.ui.components.DrawerContent
import com.example.ui.screens.agents.AgentsScreen
import com.example.ui.screens.agents.AgentsViewModel
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.compare.CompareScreen
import com.example.ui.screens.compare.CompareViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.knowledge.KnowledgeScreen
import com.example.ui.screens.knowledge.KnowledgeViewModel
import com.example.ui.screens.memory.MemoryScreen
import com.example.ui.screens.memory.MemoryViewModel
import com.example.ui.screens.plugins.PluginsScreen
import com.example.ui.screens.plugins.PluginsViewModel
import com.example.ui.screens.providers.ProvidersScreen
import com.example.ui.screens.providers.ProvidersViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.screens.workflows.WorkflowScreen
import com.example.ui.screens.workflows.WorkflowViewModel
import com.example.ui.screens.workspace.WorkspaceScreen
import com.example.ui.screens.workspace.WorkspaceViewModel
import com.example.ui.theme.OmniaMaxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var voiceAssistantManager: VoiceAssistantManager
    private lateinit var deviceControlExecutor: DeviceControlExecutor
    private lateinit var contactResolver: ContactResolver
    private lateinit var messagingDispatcher: MessagingDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val securePrefs = SecurePreferencesManager(applicationContext)
        val appPrefs = AppPreferencesRepository(applicationContext)

        val chatRepository = ChatRepository(database.chatDao())
        val providerRepository = ProviderRepository(database.providerDao(), securePrefs, appPrefs)
        val agentRepository = AgentRepository(database.agentTaskDao())
        val memoryRepository = MemoryRepository(database.memoryDao())

        deviceControlExecutor = DeviceControlExecutor(applicationContext)
        contactResolver = ContactResolver(applicationContext)
        messagingDispatcher = MessagingDispatcher(applicationContext)
        voiceAssistantManager = VoiceAssistantManager(applicationContext)

        setContent {
            val settingsViewModel = remember { SettingsViewModel(chatRepository, providerRepository, appPrefs) }
            val settingsState by settingsViewModel.uiState.collectAsState()

            OmniaMaxTheme(darkTheme = settingsState.darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val chatViewModel = remember { ChatViewModel(chatRepository, providerRepository, appPrefs) }
                    val providersViewModel = remember { ProvidersViewModel(providerRepository, appPrefs) }
                    val agentsViewModel = remember { AgentsViewModel(agentRepository, providerRepository) }
                    val compareViewModel = remember { CompareViewModel(providerRepository) }
                    val workflowViewModel = remember { WorkflowViewModel(providerRepository) }
                    val workspaceViewModel = remember { WorkspaceViewModel(providerRepository) }
                    val knowledgeViewModel = remember { KnowledgeViewModel(database.documentDao(), providerRepository) }
                    val memoryViewModel = remember { MemoryViewModel(memoryRepository) }
                    val pluginsViewModel = remember { PluginsViewModel() }

                    val chatUiState by chatViewModel.uiState.collectAsState()

                    var currentScreen by remember { mutableStateOf("welcome") }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    // Voice Assistant State
                    var isAssistantOverlayVisible by remember { mutableStateOf(false) }
                    val assistantState by voiceAssistantManager.assistantState.collectAsState()
                    val transcript by voiceAssistantManager.lastTranscript.collectAsState()
                    val spokenText by voiceAssistantManager.spokenText.collectAsState()

                    var pendingMessageCommand by remember { mutableStateOf<AssistantCommand.PrepareMessage?>(null) }
                    var resolvedContacts by remember { mutableStateOf<List<ResolvedContact>>(emptyList()) }
                    var lastExecutionResult by remember { mutableStateOf<CommandExecutionResult?>(null) }

                    // Voice Assistant Command Callback
                    voiceAssistantManager.onCommandRecognized = { text ->
                        val command = AssistantCommandParser.parseCommand(text)
                        when (command) {
                            is AssistantCommand.PrepareMessage -> {
                                pendingMessageCommand = command
                                resolvedContacts = contactResolver.resolveContact(command.recipientName)
                                voiceAssistantManager.setState(AssistantState.CONFIRMATION_REQUIRED)
                                voiceAssistantManager.speak("Ready to send message to ${command.recipientName} on ${command.platform.displayName}. Say Send or tap button to confirm.")
                            }
                            is AssistantCommand.GeneralAIQuery -> {
                                voiceAssistantManager.setState(AssistantState.PROCESSING)
                                chatViewModel.sendMessage(command.prompt)
                                voiceAssistantManager.speak("Asking MAX AI for ${command.prompt}")
                                lastExecutionResult = CommandExecutionResult(true, "Query sent to MAX AI", "Processing AI response.")
                                voiceAssistantManager.setState(AssistantState.SUCCESS)
                            }
                            else -> {
                                val result = deviceControlExecutor.execute(command)
                                lastExecutionResult = result
                                if (result.spokenResponse.isNotBlank()) {
                                    voiceAssistantManager.speak(result.spokenResponse)
                                }
                                voiceAssistantManager.setState(if (result.isSuccess) AssistantState.SUCCESS else AssistantState.ERROR)
                            }
                        }
                    }

                    BackHandler(enabled = drawerState.isOpen || currentScreen != "home") {
                        if (drawerState.isOpen) {
                            scope.launch { drawerState.close() }
                        } else if (currentScreen != "home") {
                            currentScreen = "home"
                        }
                    }

                    if (currentScreen == "welcome") {
                        WelcomeScreen(
                            onGetStarted = { currentScreen = "home" },
                            onSignIn = { currentScreen = "home" }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    threads = chatUiState.threads,
                                    activeThreadId = chatUiState.activeThread?.id,
                                    onNewChatClicked = {
                                        chatViewModel.createNewThread()
                                        currentScreen = "chat"
                                        scope.launch { drawerState.close() }
                                    },
                                    onThreadSelected = { id ->
                                        chatViewModel.selectThread(id)
                                        currentScreen = "chat"
                                        scope.launch { drawerState.close() }
                                    },
                                    onThreadDeleted = { id ->
                                        chatViewModel.deleteThread(id)
                                    },
                                    onNavigate = { destination ->
                                        currentScreen = destination
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Scaffold(
                                    bottomBar = {
                                        if (currentScreen in listOf("home", "chat", "agents", "settings", "assistant_settings")) {
                                            NavigationBar(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ) {
                                                NavigationBarItem(
                                                    selected = currentScreen == "home",
                                                    onClick = { currentScreen = "home" },
                                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                                    label = { Text("Home") }
                                                )
                                                NavigationBarItem(
                                                    selected = currentScreen == "chat",
                                                    onClick = { currentScreen = "chat" },
                                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                                                    label = { Text("Chat") }
                                                )
                                                NavigationBarItem(
                                                    selected = currentScreen == "agents",
                                                    onClick = { currentScreen = "agents" },
                                                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "Agents") },
                                                    label = { Text("Agents") }
                                                )
                                                NavigationBarItem(
                                                    selected = currentScreen == "assistant_settings",
                                                    onClick = { currentScreen = "assistant_settings" },
                                                    icon = { Icon(Icons.Default.Mic, contentDescription = "Assistant") },
                                                    label = { Text("MAX Voice") }
                                                )
                                                NavigationBarItem(
                                                    selected = currentScreen == "settings",
                                                    onClick = { currentScreen = "settings" },
                                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                                    label = { Text("Settings") }
                                                )
                                            }
                                        }
                                    }
                                ) { bottomPadding ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottomPadding)
                                    ) {
                                        AnimatedContent(
                                            targetState = currentScreen,
                                            transitionSpec = {
                                                fadeIn(animationSpec = tween(220)) togetherWith
                                                        fadeOut(animationSpec = tween(180))
                                            },
                                            label = "screen_transition"
                                        ) { targetScreen ->
                                            when (targetScreen) {
                                                "home" -> HomeScreen(
                                                    activeModelId = chatUiState.selectedModelId,
                                                    recentThreads = chatUiState.threads,
                                                    onNewChat = {
                                                        chatViewModel.createNewThread()
                                                        currentScreen = "chat"
                                                    },
                                                    onOpenThread = { id ->
                                                        chatViewModel.selectThread(id)
                                                        currentScreen = "chat"
                                                    },
                                                    onOpenService = { service ->
                                                        when (service) {
                                                            "AI Chat", "chat" -> currentScreen = "chat"
                                                            "Deep Research", "agents" -> currentScreen = "agents"
                                                            "Code Assistant", "workspace" -> currentScreen = "workspace"
                                                            "Document Analysis", "knowledge" -> currentScreen = "knowledge"
                                                            "Web Search", "plugins" -> currentScreen = "plugins"
                                                            else -> currentScreen = "chat"
                                                        }
                                                    },
                                                    onOpenSettings = { currentScreen = "settings" }
                                                )
                                                "chat" -> ChatScreen(
                                                    viewModel = chatViewModel,
                                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                                )
                                                "compare" -> CompareScreen(
                                                    viewModel = compareViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "agents" -> AgentsScreen(
                                                    viewModel = agentsViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "workflows" -> WorkflowScreen(
                                                    viewModel = workflowViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "workspace" -> WorkspaceScreen(
                                                    viewModel = workspaceViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "knowledge" -> KnowledgeScreen(
                                                    viewModel = knowledgeViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "memory" -> MemoryScreen(
                                                    viewModel = memoryViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "plugins" -> PluginsScreen(
                                                    viewModel = pluginsViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "providers" -> ProvidersScreen(
                                                    viewModel = providersViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "settings" -> SettingsScreen(
                                                    viewModel = settingsViewModel,
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                                "assistant_settings" -> AssistantSettingsScreen(
                                                    onNavigateBack = { currentScreen = "home" }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Floating Mic Button to trigger MAX Assistant Overlay
                                if (currentScreen in listOf("home", "chat", "agents")) {
                                    FloatingActionButton(
                                        onClick = {
                                            isAssistantOverlayVisible = !isAssistantOverlayVisible
                                            if (isAssistantOverlayVisible) {
                                                voiceAssistantManager.startListening()
                                            } else {
                                                voiceAssistantManager.stopListening()
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 16.dp, bottom = 80.dp)
                                            .size(56.dp),
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Trigger MAX Personal Assistant",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                // MAX Assistant Bottom Overlay
                                AssistantVoiceOverlay(
                                    isVisible = isAssistantOverlayVisible,
                                    assistantState = assistantState,
                                    transcript = transcript,
                                    spokenText = spokenText,
                                    pendingMessageCommand = pendingMessageCommand,
                                    resolvedContacts = resolvedContacts,
                                    lastExecutionResult = lastExecutionResult,
                                    onStartListening = { voiceAssistantManager.startListening() },
                                    onStopListening = { voiceAssistantManager.stopListening() },
                                    onConfirmSendMessage = { contact, messageText ->
                                        pendingMessageCommand?.let { cmd ->
                                            val result = messagingDispatcher.dispatchMessage(cmd.platform, contact, messageText)
                                            lastExecutionResult = result
                                            voiceAssistantManager.speak(result.spokenResponse)
                                            pendingMessageCommand = null
                                            voiceAssistantManager.setState(if (result.isSuccess) AssistantState.SUCCESS else AssistantState.ERROR)
                                        }
                                    },
                                    onCancelMessage = {
                                        pendingMessageCommand = null
                                        voiceAssistantManager.speak("Message cancelled.")
                                        voiceAssistantManager.setState(AssistantState.IDLE)
                                    },
                                    onDismiss = {
                                        isAssistantOverlayVisible = false
                                        voiceAssistantManager.stopListening()
                                    },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceAssistantManager.destroy()
    }
}
