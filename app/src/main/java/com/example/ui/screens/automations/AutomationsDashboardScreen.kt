package com.example.ui.screens.automations

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationsDashboardScreen(
    viewModel: AutomationsViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onOpenDrawer: () -> Unit = {}
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
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("automations_menu_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = OmniaBrightCyan)
                    }
                },
                title = {
                    Column {
                        Text(
                            "MAX AUTOMATIONS",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = OmniaNeonCyan,
                            fontSize = 17.sp
                        )
                        Text(
                            "Cross-App Execution Platform",
                            fontSize = 11.sp,
                            color = OmniaTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLogs) {
                        BadgedBox(
                            badge = {
                                if (runLogs.isNotEmpty()) {
                                    Badge(containerColor = OmniaElectricBlue) {
                                        Text("${runLogs.size}", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = "Run Logs", tint = OmniaTextPrimary)
                        }
                    }
                    IconButton(onClick = onNavigateToTemplates) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Templates", tint = OmniaBrightCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OmniaMainBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.createNewWorkflow()
                    onNavigateToEditor()
                },
                containerColor = OmniaElectricBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Workflow", tint = Color.White) },
                text = { Text("New Workflow", fontWeight = FontWeight.Bold, color = Color.White) }
            )
        },
        containerColor = OmniaMainBg
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

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search workflows or target apps...", color = OmniaTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OmniaNeonCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = OmniaTextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OmniaElectricBlue,
                    unfocusedBorderColor = OmniaSubtleBorder,
                    focusedContainerColor = OmniaCardBg,
                    unfocusedContainerColor = OmniaCardBg,
                    focusedTextColor = OmniaTextPrimary,
                    unfocusedTextColor = OmniaTextPrimary,
                    cursorColor = OmniaNeonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterChipsRow(
                selectedFilter = selectedFilter,
                onSelectFilter = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = lastSummary != null) {
                lastSummary?.let { summary ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = OmniaElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OmniaNeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                summary,
                                color = OmniaTextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearSummary() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = OmniaTextMuted)
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
        color = OmniaCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricItem(label = "Total", value = "$totalCount", color = OmniaBrightCyan)
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = OmniaSubtleBorder)
            MetricItem(label = "Active", value = "$activeCount", color = OmniaSuccessGreen)
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = OmniaSubtleBorder)
            MetricItem(label = "Runs", value = "$totalRuns", color = OmniaElectricBlue)
            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = OmniaSubtleBorder)
            MetricItem(label = "Success", value = "$successRate%", color = OmniaNeonCyan)
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = OmniaTextMuted)
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

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(key) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OmniaElectricBlue,
                    selectedLabelColor = Color.White,
                    containerColor = OmniaCardBg,
                    labelColor = OmniaTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = OmniaSubtleBorder,
                    selectedBorderColor = OmniaElectricBlue
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
        color = OmniaCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (workflow.isEnabled) OmniaElectricBlue.copy(alpha = 0.5f) else OmniaSubtleBorder
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OmniaElevatedSurface)
                            .border(1.dp, OmniaSubtleBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            workflow.targetApp.displayName.take(2).uppercase(),
                            color = OmniaBrightCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            workflow.name,
                            color = OmniaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            workflow.description,
                            color = OmniaTextSecondary,
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
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OmniaElectricBlue,
                        uncheckedThumbColor = OmniaTextMuted,
                        uncheckedTrackColor = OmniaElevatedSurface
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
                    color = OmniaElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WHEN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OmniaNeonCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(workflow.triggerType.displayName, fontSize = 10.sp, color = OmniaTextPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OmniaElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("THEN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OmniaRoyalBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${workflow.actions.size} step(s)", fontSize = 10.sp, color = OmniaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onRun,
                    enabled = !isExecuting,
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = OmniaTextMuted)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(OmniaElevatedSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Workflow", color = OmniaTextPrimary) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OmniaNeonCyan) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = OmniaErrorRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = OmniaErrorRed) }
                        )
                    }
                }
            }

            if (workflow.lastRunMs > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(workflow.lastRunMs))
                Text(
                    "Last run: $dateStr • Total runs: ${workflow.runCount} (Success: ${workflow.successCount})",
                    fontSize = 10.sp,
                    color = OmniaTextMuted
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
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = OmniaBrightCyan.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            if (searchQuery.isEmpty()) "No Automations Configured" else "No matching automations",
            color = OmniaTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Create custom WHEN-IF-THEN cross-app workflows or install pre-configured templates.",
            color = OmniaTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onOpenTemplates,
                colors = ButtonDefaults.outlinedButtonColors(containerColor = OmniaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan)
            ) {
                Text("Explore Templates", color = OmniaNeonCyan)
            }
            Button(
                onClick = onCreateNew,
                colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue)
            ) {
                Text("Build Workflow", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
