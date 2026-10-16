package com.example.ui.screens.automations

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationsDashboardScreen(
    viewModel: AutomationsViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val automations by viewModel.automations.collectAsState()
    val runLogs by viewModel.runLogs.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val lastSummary by viewModel.lastExecutionSummary.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAutomations = remember(automations, searchQuery, selectedFilter) {
        automations.filter { wf ->
            val matchesSearch = wf.name.contains(searchQuery, ignoreCase = true) ||
                    wf.description.contains(searchQuery, ignoreCase = true) ||
                    wf.targetApp.displayName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "MANUAL" -> wf.triggerType == TriggerType.MANUAL
                "SCHEDULED" -> wf.triggerType == TriggerType.SCHEDULED
                "SYSTEM" -> wf.triggerType == TriggerType.SYSTEM_EVENT
                "WEBHOOK" -> wf.triggerType == TriggerType.WEBHOOK
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val totalRuns = automations.fold(0) { acc, wf -> acc + wf.runCount }
    val totalSuccess = automations.fold(0) { acc, wf -> acc + wf.successCount }
    val successRate = if (totalRuns > 0) ((totalSuccess.toDouble() / totalRuns) * 100).toInt() else 100
    val activeCount = automations.count { it.isEnabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MAX AUTOMATIONS",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF00F2FE)
                        )
                        Text(
                            "Cross-App Execution Platform",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLogs) {
                        BadgedBox(
                            badge = {
                                if (runLogs.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF00F2FE)) {
                                        Text("${runLogs.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = "Run Logs", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onNavigateToTemplates) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Templates", tint = Color(0xFF00F2FE))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0E15))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.createNewWorkflow()
                    onNavigateToEditor()
                },
                containerColor = Color(0xFF00F2FE),
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Workflow") },
                text = { Text("New Workflow", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color(0xFF0D0E15)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            MetricsSummaryBar(
                totalCount = automations.size,
                activeCount = activeCount,
                successRate = successRate,
                totalRuns = totalRuns
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search workflows or target apps...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00F2FE)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F2FE),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color(0xFF161824),
                    unfocusedContainerColor = Color(0xFF161824),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterChipsRow(
                selectedFilter = selectedFilter,
                onSelectFilter = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = lastSummary != null) {
                lastSummary?.let { summary ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00F2FE).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00F2FE))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                summary,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearSummary() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray)
                            }
                        }
                    }
                }
            }

            if (filteredAutomations.isEmpty()) {
                EmptyStateView(
                    searchQuery = searchQuery,
                    onOpenTemplates = onNavigateToTemplates,
                    onCreateNew = {
                        viewModel.createNewWorkflow()
                        onNavigateToEditor()
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredAutomations, key = { it.id }) { workflow ->
                        WorkflowCard(
                            workflow = workflow,
                            isExecuting = isExecuting,
                            onToggle = { viewModel.toggleEnabled(workflow) },
                            onRun = { viewModel.executeWorkflow(workflow) },
                            onEdit = {
                                viewModel.selectWorkflowForEdit(workflow)
                                onNavigateToEditor()
                            },
                            onDelete = { viewModel.deleteWorkflow(workflow.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsSummaryBar(
    totalCount: Int,
    activeCount: Int,
    successRate: Int,
    totalRuns: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF161824),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF00F2FE), Color(0xFF9D50BB)))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricItem(label = "Total", value = "$totalCount", color = Color(0xFF00F2FE))
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            MetricItem(label = "Active", value = "$activeCount", color = Color(0xFF4CAF50))
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            MetricItem(label = "Total Runs", value = "$totalRuns", color = Color(0xFFFF9800))
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            MetricItem(label = "Success", value = "$successRate%", color = Color(0xFFE91E63))
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    val filters = listOf(
        "ALL" to "All",
        "MANUAL" to "Manual",
        "SCHEDULED" to "Scheduled",
        "SYSTEM" to "System",
        "WEBHOOK" to "Webhooks"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(key) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00F2FE),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF161824),
                    labelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White.copy(alpha = 0.2f),
                    selectedBorderColor = Color(0xFF00F2FE)
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun WorkflowCard(
    workflow: AutomationWorkflow,
    isExecuting: Boolean,
    onToggle: () -> Unit,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        color = Color(0xFF161824),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (workflow.isEnabled) Color(0xFF00F2FE).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF212538)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            workflow.targetApp.displayName.take(2).uppercase(),
                            color = Color(0xFF00F2FE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            workflow.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            workflow.description,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Switch(
                    checked = workflow.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFF00F2FE),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF212538)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00F2FE).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WHEN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F2FE))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(workflow.triggerType.displayName, fontSize = 10.sp, color = Color.White)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF9D50BB).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9D50BB).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("THEN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D50BB))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${workflow.actions.size} step(s)", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onRun,
                    enabled = !isExecuting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F2FE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF212538))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Workflow", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00F2FE)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) }
                        )
                    }
                }
            }

            if (workflow.lastRunMs > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(workflow.lastRunMs))
                Text(
                    "Last run: $dateStr • Runs: ${workflow.runCount} (Success: ${workflow.successCount})",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    searchQuery: String,
    onOpenTemplates: () -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF00F2FE).copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (searchQuery.isEmpty()) "No Automations Found" else "No matching automations",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Create custom WHEN-IF-THEN cross-app workflows or install from the Template Gallery.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onOpenTemplates,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161824)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE))
            ) {
                Text("Explore Templates", color = Color(0xFF00F2FE))
            }
            Button(
                onClick = onCreateNew,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE))
            ) {
                Text("Build Workflow", color = Color.Black)
            }
        }
    }
}
