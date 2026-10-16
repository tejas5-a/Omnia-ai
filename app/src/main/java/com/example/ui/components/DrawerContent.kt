package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread

@Composable
fun DrawerContent(
    threads: List<ChatThread>,
    activeThreadId: String?,
    onNewChatClicked: () -> Unit,
    onThreadSelected: (String) -> Unit,
    onThreadDeleted: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredThreads = threads.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // App Brand Header with Pulsing AI Orb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiOrbView(size = 36.dp, isPulsing = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OMNIA MAX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AI Agent Operating System",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // New Chat Button
            Button(
                onClick = {
                    onNewChatClicked()
                    onNavigate("chat")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_chat_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Conversation", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("chat_search_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Conversations",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Threads List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredThreads, key = { it.id }) { thread ->
                    val isSelected = thread.id == activeThreadId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                onThreadSelected(thread.id)
                                onNavigate("chat")
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = thread.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(onClick = { onThreadDeleted(thread.id) }, modifier = Modifier.size(22.dp)) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Suite Links List
            LazyColumn(modifier = Modifier.height(200.dp)) {
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Multi-Model Compare", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("compare") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Multi-Agent Engine", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("agents") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Visual Workflows", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("workflows") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Coding Workspace", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("workspace") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Knowledge Center", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("knowledge") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Project Memory", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("memory") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Plugin Marketplace", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("plugins") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("API Provider Hub", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("providers") },
                        modifier = Modifier.height(40.dp)
                    )
                }
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Settings & Usage", fontSize = 13.sp) },
                        selected = false,
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }
}
