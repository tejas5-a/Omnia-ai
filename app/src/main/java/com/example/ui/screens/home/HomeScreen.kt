package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread
import com.example.ui.components.AiOrbView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeModelId: String,
    recentThreads: List<ChatThread>,
    onNewChat: () -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenService: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("home_menu_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = OmniaBrightCyan)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = OmniaElevatedSurface,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Ω", color = OmniaBrightCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "OMNIA MAX",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OmniaTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OmniaTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OmniaMainBg,
                    titleContentColor = OmniaTextPrimary
                )
            )
        },
        containerColor = OmniaMainBg,
        modifier = modifier
    ) { innerPadding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OmniaMainBg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Greeting
                Text(
                    text = "Welcome to OMNIA MAX",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary
                )
                Text(
                    text = "High-performance AI assistant & multi-app automation system.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OmniaTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Active Model Pill
                Surface(
                    color = OmniaElevatedSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OmniaSuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active Engine: $activeModelId",
                            fontSize = 12.sp,
                            color = OmniaNeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Action: Start New Conversation Card (No redundant bottomBar)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaCardBorderGradient),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewChat() }
                        .testTag("home_new_chat_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AiOrbView(size = 44.dp, isPulsing = true)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start New Conversation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OmniaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chat, brainstorm, write code, or execute cross-app workflows",
                                fontSize = 12.sp,
                                color = OmniaTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = OmniaNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Quick Action Service Chips
                Text(
                    text = "Quick Actions & Services",
                    style = MaterialTheme.typography.labelLarge,
                    color = OmniaTextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                val serviceChips = listOf(
                    Pair("AI Chat", Icons.Default.Chat),
                    Pair("MAX Automations", Icons.Default.Bolt),
                    Pair("Deep Research", Icons.Default.TravelExplore),
                    Pair("Code Assistant", Icons.Default.Code),
                    Pair("Document Analysis", Icons.Default.Description),
                    Pair("Web Search", Icons.Default.Language)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(serviceChips) { (title, icon) ->
                        Surface(
                            color = OmniaCardBg,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                            modifier = Modifier.clickable { onOpenService(title) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (title == "MAX Automations") OmniaNeonCyan else OmniaElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OmniaTextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Conversations Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Conversations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentThreads.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = OmniaCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No conversations yet. Tap above to start one!", color = OmniaTextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    recentThreads.take(4).forEach { thread ->
                        Surface(
                            onClick = { onOpenThread(thread.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = OmniaCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = OmniaElectricBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = thread.title,
                                            fontWeight = FontWeight.Bold,
                                            color = OmniaTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Model: ${thread.modelId}",
                                            fontSize = 11.sp,
                                            color = OmniaTextMuted
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OmniaTextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AI Workflows & Tools Grid
                Text(
                    text = "AI Workflows & Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                val tools = listOf(
                    ToolGridItem("MAX Automations", "Automate WhatsApp, YouTube, Spotify, and system workflows.", Icons.Default.Bolt, "automations"),
                    ToolGridItem("Generate Content", "Create essays, social posts, or marketing copy.", Icons.Default.AutoAwesome, "agents"),
                    ToolGridItem("Document Creation", "Draft technical specifications & docs.", Icons.Default.Description, "knowledge"),
                    ToolGridItem("Coding Assistant", "Debug code, write tests, or refactor logic.", Icons.Default.Code, "workspace")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tools.chunked(2).forEach { rowTools ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowTools.forEach { tool ->
                                Surface(
                                    onClick = { onOpenService(tool.targetScreen) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = OmniaCardBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(125.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = OmniaElevatedSurface,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = tool.icon,
                                                        contentDescription = null,
                                                        tint = OmniaNeonCyan,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = OmniaTextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = tool.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = OmniaTextPrimary
                                            )
                                            Text(
                                                text = tool.description,
                                                fontSize = 10.sp,
                                                color = OmniaTextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

data class ToolGridItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val targetScreen: String
)
