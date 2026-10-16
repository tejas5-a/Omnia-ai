package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.SecurePreferencesManager
import com.example.data.repository.*
import com.example.ui.components.DrawerContent
import com.example.ui.screens.agents.AgentsScreen
import com.example.ui.screens.agents.AgentsViewModel
import com.example.ui.screens.automations.AutomationLogsScreen
import com.example.ui.screens.automations.AutomationsDashboardScreen
import com.example.ui.screens.automations.AutomationsViewModel
import com.example.ui.screens.automations.TemplateGalleryScreen
import com.example.ui.screens.automations.WorkflowEditorScreen
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
import com.example.ui.screens.workflows.WorkflowScreen
import com.example.ui.screens.workflows.WorkflowViewModel
import com.example.ui.screens.workspace.WorkspaceScreen
import com.example.ui.screens.workspace.WorkspaceViewModel
import com.example.ui.theme.*
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

            val settingsState by settingsViewModel.uiState.collectAsState()
            val userPrefs by appPrefs.userPreferences.collectAsState()
            val chatUiState by chatViewModel.uiState.collectAsState()

            // Restore last active screen or launch directly into "chat" (bypassing welcome screen)
            val initialScreen = remember {
                val saved = userPrefs.lastScreen
                if (saved.isNotBlank() && saved != "welcome") saved else "chat"
            }
            var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            fun navigateTo(destination: String) {
                currentScreen = destination
                appPrefs.updateLastScreen(destination)
                scope.launch { drawerState.close() }
            }

            BackHandler(enabled = drawerState.isOpen || currentScreen != "chat") {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (currentScreen != "chat") {
                    navigateTo("chat")
                }
            }

            OmniaMaxTheme(darkTheme = userPrefs.darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                threads = chatUiState.threads,
                                activeThreadId = chatUiState.activeThread?.id,
                                activeScreen = currentScreen,
                                onNewChatClicked = {
                                    chatViewModel.createNewThread()
                                    navigateTo("chat")
                                },
                                onThreadSelected = { id ->
                                    chatViewModel.selectThread(id)
                                    navigateTo("chat")
                                },
                                onThreadRenamed = { id, newTitle ->
                                    chatViewModel.renameThread(id, newTitle)
                                },
                                onThreadDeleted = { id ->
                                    chatViewModel.deleteThread(id)
                                },
                                onNavigate = { destination ->
                                    navigateTo(destination)
                                }
                            )
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Scaffold(
                                contentWindowInsets = WindowInsets(0, 0, 0, 0)
                            ) { innerPadding ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    color = OmniaMainBg
                                ) {
                                    AnimatedContent(
                                        targetState = currentScreen,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(180)) togetherWith
                                                    fadeOut(animationSpec = tween(140))
                                        },
                                        label = "screen_transition"
                                    ) { targetScreen ->
                                        when (targetScreen) {
                                            "home" -> HomeScreen(
                                                activeModelId = chatUiState.selectedModelId,
                                                recentThreads = chatUiState.threads,
                                                onNewChat = {
                                                    chatViewModel.createNewThread()
                                                    navigateTo("chat")
                                                },
                                                onOpenThread = { id ->
                                                    chatViewModel.selectThread(id)
                                                    navigateTo("chat")
                                                },
                                                onOpenService = { service ->
                                                    when (service) {
                                                        "MAX Automations", "automations" -> navigateTo("automations")
                                                        "AI Chat", "chat" -> navigateTo("chat")
                                                        "Deep Research", "agents" -> navigateTo("agents")
                                                        "Code Assistant", "workspace" -> navigateTo("workspace")
                                                        "Document Analysis", "knowledge" -> navigateTo("knowledge")
                                                        "Web Search", "plugins" -> navigateTo("plugins")
                                                        else -> navigateTo("chat")
                                                    }
                                                },
                                                onOpenSettings = { navigateTo("settings") },
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                            "automations" -> AutomationsDashboardScreen(
                                                viewModel = automationsViewModel,
                                                onNavigateToEditor = { navigateTo("automations_editor") },
                                                onNavigateToTemplates = { navigateTo("automations_templates") },
                                                onNavigateToLogs = { navigateTo("automations_logs") },
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                            "automations_editor" -> WorkflowEditorScreen(
                                                viewModel = automationsViewModel,
                                                onNavigateBack = { navigateTo("automations") }
                                            )
                                            "automations_templates" -> TemplateGalleryScreen(
                                                viewModel = automationsViewModel,
                                                onNavigateBack = { navigateTo("automations") }
                                            )
                                            "automations_logs" -> AutomationLogsScreen(
                                                viewModel = automationsViewModel,
                                                onNavigateBack = { navigateTo("automations") }
                                            )
                                            "chat" -> ChatScreen(
                                                viewModel = chatViewModel,
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                            "compare" -> CompareScreen(
                                                viewModel = compareViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "agents" -> AgentsScreen(
                                                viewModel = agentsViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "workflows" -> WorkflowScreen(
                                                viewModel = workflowViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "workspace" -> WorkspaceScreen(
                                                viewModel = workspaceViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "knowledge" -> KnowledgeScreen(
                                                viewModel = knowledgeViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "memory" -> MemoryScreen(
                                                viewModel = memoryViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "plugins" -> PluginsScreen(
                                                viewModel = pluginsViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "providers" -> ProvidersScreen(
                                                viewModel = providersViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            "settings" -> SettingsScreen(
                                                viewModel = settingsViewModel,
                                                onNavigateBack = { navigateTo("home") }
                                            )
                                            else -> HomeScreen(
                                                activeModelId = chatUiState.selectedModelId,
                                                recentThreads = chatUiState.threads,
                                                onNewChat = {
                                                    chatViewModel.createNewThread()
                                                    navigateTo("chat")
                                                },
                                                onOpenThread = { id ->
                                                    chatViewModel.selectThread(id)
                                                    navigateTo("chat")
                                                },
                                                onOpenService = { navigateTo("chat") },
                                                onOpenSettings = { navigateTo("settings") }
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

