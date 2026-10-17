package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.ui.theme.*

data class SuiteDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val isPrimary: Boolean = false
)

@Composable
fun DrawerContent(
    threads: List<ChatThread>,
    activeThreadId: String?,
    activeScreen: String = "chat",
    onNewChatClicked: () -> Unit,
    onThreadSelected: (String) -> Unit,
    onThreadRenamed: (String, String) -> Unit = { _, _ -> },
    onThreadDeleted: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var threadToDeleteId by remember { mutableStateOf<String?>(null) }
    var threadToRename by remember { mutableStateOf<ChatThread?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredThreads = threads.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    val suiteDestinations = listOf(
        SuiteDestination("chat", "AI Chat & Models", Icons.Default.Chat, isPrimary = true),
        SuiteDestination("automations", "MAX Automations", Icons.Default.Bolt, isPrimary = true),
        SuiteDestination("agents", "Multi-Agent Engine", Icons.Default.SmartToy),
        SuiteDestination("compare", "Multi-Model Compare", Icons.Default.Compare),
        SuiteDestination("workspace", "Coding Workspace", Icons.Default.Code),
        SuiteDestination("knowledge", "Knowledge Center", Icons.Default.MenuBook),
        SuiteDestination("memory", "Persistent Memory", Icons.Default.Psychology),
        SuiteDestination("plugins", "Plugin Marketplace", Icons.Default.Extension),
        SuiteDestination("providers", "API Provider Hub", Icons.Default.Hub),
        SuiteDestination("settings", "Settings & Appearance", Icons.Default.Settings)
    )

    ModalDrawerSheet(
        drawerContainerColor = ChatDarkDrawerBg,
        drawerContentColor = ChatDarkTextPrimary,
        modifier = modifier
            .widthIn(max = 310.dp)
            .fillMaxWidth(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // App Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiOrbView(size = 32.dp, isPulsing = true)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "OMNIA MAX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Smarter AI. Greater Possibilities.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChatDarkTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // New Conversation Action Button
            Button(
                onClick = {
                    onNewChatClicked()
                    onNavigate("chat")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("new_chat_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar for conversations
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...", fontSize = 12.sp, color = ChatDarkTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(16.dp),
                        tint = ChatDarkTextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("chat_search_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Recent Conversations",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ChatDarkTextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                if (filteredThreads.isEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations" else "No conversations yet",
                            color = ChatDarkTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                } else {
                    items(filteredThreads, key = { it.id }) { thread ->
                        val isSelected = thread.id == activeThreadId && activeScreen == "chat"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onThreadSelected(thread.id)
                                    onNavigate("chat")
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onBackground else ChatDarkTextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = thread.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else ChatDarkTextSecondary
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            threadToRename = thread
                                            renameText = thread.title
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename",
                                            modifier = Modifier.size(13.dp),
                                            tint = ChatDarkTextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { threadToDeleteId = thread.id },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(14.dp),
                                            tint = ChatDarkTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Text(
                        text = "OMNIA Suite & Features",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ChatDarkTextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                items(suiteDestinations, key = { it.route }) { dest ->
                    val isSelected = activeScreen == dest.route
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigate(dest.route) },
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onBackground else ChatDarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dest.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else ChatDarkTextSecondary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    threadToDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { threadToDeleteId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Conversation?", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove this conversation from your history.", color = ChatDarkTextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    onThreadDeleted(id)
                    threadToDeleteId = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDeleteId = null }) {
                    Text("Cancel", color = ChatDarkTextSecondary)
                }
            }
        )
    }

    // Rename Conversation Dialog
    threadToRename?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToRename = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Rename Conversation", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onThreadRenamed(thread.id, renameText.trim())
                        }
                        threadToRename = null
                    },
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToRename = null }) {
                    Text("Cancel", color = ChatDarkTextSecondary)
                }
            }
        )
    }
}
