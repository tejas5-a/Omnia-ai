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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.example.ui.screens.automations.AutomationLogsScreen
import com.example.ui.screens.automations.AutomationsDashboardScreen
import com.example.ui.screens.automations.AutomationsViewModel
import com.example.ui.screens.automations.TemplateGalleryScreen
import com.example.ui.screens.automations.WorkflowEditorScreen
import com.example.ui.theme.OmniaMaxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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
                    val automationsViewModel = remember { AutomationsViewModel(application) }

                    val chatUiState by chatViewModel.uiState.collectAsState()

                    var currentScreen by remember { mutableStateOf("welcome") }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

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
                                        if (currentScreen in listOf("home", "chat", "agents", "automations", "settings")) {
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
                                                    selected = currentScreen == "automations",
                                                    onClick = { currentScreen = "automations" },
                                                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Automations") },
                                                    label = { Text("Automations") }
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
                                                            "MAX Automations", "automations" -> currentScreen = "automations"
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
                                                "automations" -> AutomationsDashboardScreen(
                                                    viewModel = automationsViewModel,
                                                    onNavigateToEditor = { currentScreen = "automations_editor" },
                                                    onNavigateToTemplates = { currentScreen = "automations_templates" },
                                                    onNavigateToLogs = { currentScreen = "automations_logs" }
                                                )
                                                "automations_editor" -> WorkflowEditorScreen(
                                                    viewModel = automationsViewModel,
                                                    onNavigateBack = { currentScreen = "automations" }
                                                )
                                                "automations_templates" -> TemplateGalleryScreen(
                                                    viewModel = automationsViewModel,
                                                    onNavigateBack = { currentScreen = "automations" }
                                                )
                                                "automations_logs" -> AutomationLogsScreen(
                                                    viewModel = automationsViewModel,
                                                    onNavigateBack = { currentScreen = "automations" }
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
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
