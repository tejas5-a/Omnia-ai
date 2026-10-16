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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditorScreen(
    viewModel: AutomationsViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedWorkflow by viewModel.selectedWorkflow.collectAsState()

    var name by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.name ?: "New Workflow") }
    var description by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.description ?: "Custom cross-app automation") }
    var triggerType by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.triggerType ?: TriggerType.MANUAL) }
    var targetApp by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.targetApp ?: CrossAppTarget.SYSTEM) }
    var triggerTime by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.triggerConfig?.time ?: "09:00") }
    var webhookUrl by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.triggerConfig?.webhookUrl ?: "https://api.omnia.com/webhook/v1") }

    var conditions by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.conditions ?: emptyList()) }
    var actions by remember(selectedWorkflow) { mutableStateOf(selectedWorkflow?.actions ?: listOf(
        AutomationAction(
            id = UUID.randomUUID().toString(),
            actionType = "OPEN_APP",
            targetApp = targetApp,
            parameters = mapOf("message" to "Triggered from OMNIA MAX")
        )
    )) }

    var showSimulatorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WORKFLOW EDITOR", fontWeight = FontWeight.Bold, color = Color(0xFF00F2FE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { showSimulatorDialog = true }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00F2FE), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate", color = Color(0xFF00F2FE), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val workflowToSave = AutomationWorkflow(
                                id = selectedWorkflow?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                iconName = targetApp.name,
                                isEnabled = selectedWorkflow?.isEnabled ?: true,
                                triggerType = triggerType,
                                triggerConfig = TriggerConfig(time = triggerTime, webhookUrl = webhookUrl),
                                conditions = conditions,
                                actions = actions,
                                targetApp = targetApp,
                                createdAt = selectedWorkflow?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveSelectedWorkflow(workflowToSave)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0E15))
            )
        },
        containerColor = Color(0xFF0D0E15)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161824),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General Information", color = Color(0xFF00F2FE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Workflow Name", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = editorTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = editorTextFieldColors()
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161824),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Target Application Integration", color = Color(0xFF00F2FE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CrossAppTarget.values()) { app ->
                                val isSelected = targetApp == app
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { targetApp = app },
                                    color = if (isSelected) Color(0xFF00F2FE).copy(alpha = 0.2f) else Color(0xFF212538),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F2FE) else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            app.displayName,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color(0xFF00F2FE) else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161824),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1. WHEN (Trigger Event)", color = Color(0xFF00F2FE), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(TriggerType.values()) { type ->
                                val isSelected = triggerType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { triggerType = type },
                                    label = { Text(type.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00F2FE),
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF212538),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (triggerType == TriggerType.SCHEDULED) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = triggerTime,
                                onValueChange = { triggerTime = it },
                                label = { Text("Scheduled Time (HH:mm)", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = editorTextFieldColors()
                            )
                        } else if (triggerType == TriggerType.WEBHOOK) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webhookUrl,
                                onValueChange = { webhookUrl = it },
                                label = { Text("Webhook Endpoint URL", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = editorTextFieldColors()
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161824),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("2. IF (Optional Conditions)", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = {
                                conditions = conditions + AutomationCondition(
                                    id = UUID.randomUUID().toString(),
                                    field = "status",
                                    operator = "EQUALS",
                                    targetValue = "active"
                                )
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                Text("Add Condition", color = Color(0xFFFF9800), fontSize = 12.sp)
                            }
                        }

                        if (conditions.isEmpty()) {
                            Text("No conditions set (Actions execute unconditionally).", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            for ((index, cond) in conditions.withIndex()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cond.field,
                                        onValueChange = { newField ->
                                            conditions = conditions.toMutableList().apply {
                                                this[index] = cond.copy(field = newField)
                                            }
                                        },
                                        label = { Text("Field", color = Color.Gray) },
                                        modifier = Modifier.weight(1f),
                                        colors = editorTextFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = cond.targetValue,
                                        onValueChange = { newVal ->
                                            conditions = conditions.toMutableList().apply {
                                                this[index] = cond.copy(targetValue = newVal)
                                            }
                                        },
                                        label = { Text("Equals", color = Color.Gray) },
                                        modifier = Modifier.weight(1f),
                                        colors = editorTextFieldColors()
                                    )
                                    IconButton(onClick = {
                                        conditions = conditions.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161824),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9D50BB).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("3. THEN (Action Execution Steps)", color = Color(0xFF9D50BB), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = {
                                actions = actions + AutomationAction(
                                    id = UUID.randomUUID().toString(),
                                    actionType = "SEND_MESSAGE",
                                    targetApp = targetApp,
                                    parameters = mapOf("message" to "Step executed automatically")
                                )
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF9D50BB), modifier = Modifier.size(16.dp))
                                Text("Add Action Step", color = Color(0xFF9D50BB), fontSize = 12.sp)
                            }
                        }

                        for ((index, action) in actions.withIndex()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF212538)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Step ${index + 1}: ${action.actionType}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (actions.size > 1) {
                                            IconButton(onClick = {
                                                actions = actions.toMutableList().apply { removeAt(index) }
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    for ((pKey, pVal) in action.parameters) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = pVal,
                                            onValueChange = { newVal ->
                                                val newParams = action.parameters.toMutableMap()
                                                newParams[pKey] = newVal
                                                actions = actions.toMutableList().apply {
                                                    this[index] = action.copy(parameters = newParams)
                                                }
                                            },
                                            label = { Text(pKey, color = Color.Gray) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = editorTextFieldColors()
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

    if (showSimulatorDialog) {
        AlertDialog(
            onDismissRequest = { showSimulatorDialog = false },
            title = { Text("Workflow Execution Simulator", color = Color(0xFF00F2FE), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Dry-running workflow steps across target ${targetApp.displayName}...", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("WHEN: ${triggerType.displayName}", color = Color.Gray, fontSize = 11.sp)
                    Text("IF: ${if (conditions.isEmpty()) "Always True" else "${conditions.size} condition(s)"}", color = Color.Gray, fontSize = 11.sp)
                    Text("THEN: ${actions.size} action(s) planned", color = Color.Gray, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val testWf = AutomationWorkflow(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            iconName = targetApp.name,
                            isEnabled = true,
                            triggerType = triggerType,
                            triggerConfig = TriggerConfig(time = triggerTime),
                            conditions = conditions,
                            actions = actions,
                            targetApp = targetApp,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.executeWorkflow(testWf)
                        showSimulatorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE))
                ) {
                    Text("Run Live Test", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulatorDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF161824)
        )
    }
}

@Composable
private fun editorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00F2FE),
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedContainerColor = Color(0xFF212538),
    unfocusedContainerColor = Color(0xFF212538),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
