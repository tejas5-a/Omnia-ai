package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import com.example.ui.components.DrawerContent
import com.example.ui.screens.agents.AgentsScreen
import com.example.ui.screens.agents.AgentsViewModel
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.compare.CompareScreen
import com.example.ui.screens.compare.CompareViewModel
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
import com.example.ui.theme.OmniaMaxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val chatRepository = ChatRepository(database.chatDao())
        val providerRepository = ProviderRepository(database.providerDao())
        val agentRepository = AgentRepository(database.agentTaskDao())
        val memoryRepository = MemoryRepository(database.memoryDao())

        setContent {
            val settingsViewModel = remember { SettingsViewModel(chatRepository, providerRepository) }
            val settingsState by settingsViewModel.uiState.collectAsState()

            OmniaMaxTheme(darkTheme = settingsState.darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val chatViewModel = remember { ChatViewModel(chatRepository, providerRepository) }
                    val providersViewModel = remember { ProvidersViewModel(providerRepository) }
                    val agentsViewModel = remember { AgentsViewModel(agentRepository, providerRepository) }
                    val compareViewModel = remember { CompareViewModel(providerRepository) }
                    val workflowViewModel = remember { WorkflowViewModel(providerRepository) }
                    val workspaceViewModel = remember { WorkspaceViewModel(providerRepository) }
                    val knowledgeViewModel = remember { KnowledgeViewModel(database.documentDao(), providerRepository) }
                    val memoryViewModel = remember { MemoryViewModel(memoryRepository) }
                    val pluginsViewModel = remember { PluginsViewModel() }

                    val chatUiState by chatViewModel.uiState.collectAsState()

                    var currentScreen by remember { mutableStateOf("chat") }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    BackHandler(enabled = drawerState.isOpen || currentScreen != "chat") {
                        if (drawerState.isOpen) {
                            scope.launch { drawerState.close() }
                        } else if (currentScreen != "chat") {
                            currentScreen = "chat"
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                threads = chatUiState.threads,
                                activeThreadId = chatUiState.activeThread?.id,
                                onNewChatClicked = {
                                    chatViewModel.createNewThread()
                                    scope.launch { drawerState.close() }
                                },
                                onThreadSelected = { id ->
                                    chatViewModel.selectThread(id)
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
                        when (currentScreen) {
                            "chat" -> ChatScreen(
                                viewModel = chatViewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                            "compare" -> CompareScreen(
                                viewModel = compareViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "agents" -> AgentsScreen(
                                viewModel = agentsViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "workflows" -> WorkflowScreen(
                                viewModel = workflowViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "workspace" -> WorkspaceScreen(
                                viewModel = workspaceViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "knowledge" -> KnowledgeScreen(
                                viewModel = knowledgeViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "memory" -> MemoryScreen(
                                viewModel = memoryViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "plugins" -> PluginsScreen(
                                viewModel = pluginsViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "providers" -> ProvidersScreen(
                                viewModel = providersViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateBack = { currentScreen = "chat" }
                            )
                        }
                    }
                }
            }
        }
    }
}
