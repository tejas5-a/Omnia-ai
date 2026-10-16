package com.example.ui.screens.automations

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.*
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = OmniaElevatedSurface,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Workflow Builder",
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OmniaTextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { showSimulatorDialog = true }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OmniaBrightCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate", color = OmniaBrightCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OmniaMainBg)
            )
        },
        containerColor = OmniaMainBg
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
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General Information", color = OmniaNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Workflow Name", color = OmniaTextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = editorTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = OmniaTextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = editorTextFieldColors()
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Target Application Integration", color = OmniaNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CrossAppTarget.values()) { app ->
                                val isSelected = targetApp == app
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { targetApp = app },
                                    color = if (isSelected) OmniaElectricBlue.copy(alpha = 0.25f) else OmniaElevatedSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) OmniaNeonCyan else OmniaSubtleBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            app.displayName,
                                            fontSize = 12.sp,
                                            color = if (isSelected) OmniaBrightCyan else OmniaTextPrimary,
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
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1. WHEN (Trigger Event)", color = OmniaNeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                        selectedContainerColor = OmniaElectricBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = OmniaElevatedSurface,
                                        labelColor = OmniaTextSecondary
                                    )
                                )
                            }
                        }

                        if (triggerType == TriggerType.SCHEDULED) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = triggerTime,
                                onValueChange = { triggerTime = it },
                                label = { Text("Scheduled Time (HH:mm)", color = OmniaTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = editorTextFieldColors()
                            )
                        } else if (triggerType == TriggerType.WEBHOOK) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webhookUrl,
                                onValueChange = { webhookUrl = it },
                                label = { Text("Webhook Endpoint URL", color = OmniaTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = editorTextFieldColors()
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaWarningAmber.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("2. IF (Optional Conditions)", color = OmniaWarningAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = {
                                conditions = conditions + AutomationCondition(
                                    id = UUID.randomUUID().toString(),
                                    field = "status",
                                    operator = "EQUALS",
                                    targetValue = "active"
                                )
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = OmniaWarningAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Condition", color = OmniaWarningAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (conditions.isEmpty()) {
                            Text("No conditions set (Actions execute unconditionally).", fontSize = 12.sp, color = OmniaTextSecondary)
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
                                        label = { Text("Field", color = OmniaTextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = editorTextFieldColors()
                                    )
                                    OutlinedTextField(
                                        value = cond.targetValue,
                                        onValueChange = { newVal ->
                                            conditions = conditions.toMutableList().apply {
                                                this[index] = cond.copy(targetValue = newVal)
                                            }
                                        },
                                        label = { Text("Equals", color = OmniaTextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = editorTextFieldColors()
                                    )
                                    IconButton(onClick = {
                                        conditions = conditions.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = OmniaErrorRed)
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
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaElectricBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("3. THEN (Action Execution Steps)", color = OmniaBrightCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = {
                                actions = actions + AutomationAction(
                                    id = UUID.randomUUID().toString(),
                                    actionType = "SEND_MESSAGE",
                                    targetApp = targetApp,
                                    parameters = mapOf("message" to "Step executed automatically")
                                )
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = OmniaBrightCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Action Step", color = OmniaBrightCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        for ((index, action) in actions.withIndex()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = OmniaElevatedSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Step ${index + 1}: ${action.actionType}", color = OmniaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (actions.size > 1) {
                                            IconButton(onClick = {
                                                actions = actions.toMutableList().apply { removeAt(index) }
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = OmniaErrorRed, modifier = Modifier.size(16.dp))
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
                                            label = { Text(pKey, color = OmniaTextMuted, fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
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
            containerColor = OmniaCardBg,
            titleContentColor = OmniaTextPrimary,
            textContentColor = OmniaTextSecondary,
            title = { Text("Workflow Execution Simulator", color = OmniaNeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Dry-running workflow steps across target ${targetApp.displayName}...", color = OmniaTextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("WHEN: ${triggerType.displayName}", color = OmniaTextSecondary, fontSize = 11.sp)
                    Text("IF: ${if (conditions.isEmpty()) "Always True" else "${conditions.size} condition(s)"}", color = OmniaTextSecondary, fontSize = 11.sp)
                    Text("THEN: ${actions.size} action(s) planned", color = OmniaTextSecondary, fontSize = 11.sp)
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
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue)
                ) {
                    Text("Run Live Test", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulatorDialog = false }) {
                    Text("Cancel", color = OmniaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun editorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OmniaNeonCyan,
    unfocusedBorderColor = OmniaSubtleBorder,
    focusedContainerColor = OmniaElevatedSurface,
    unfocusedContainerColor = OmniaElevatedSurface,
    focusedTextColor = OmniaTextPrimary,
    unfocusedTextColor = OmniaTextPrimary,
    cursorColor = OmniaNeonCyan
)
