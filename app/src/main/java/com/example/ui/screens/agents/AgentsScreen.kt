package com.example.ui.screens.agents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    viewModel: AgentsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Active Pipeline, 1 = History

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
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Multi-Agent Orchestrator",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OmniaMainBg)
            )
        },
        containerColor = OmniaMainBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Task Prompt Input Field Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delegate Task to 6 Specialized Agents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Research → Coding → Testing → Writing → Security → Review",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniaNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.userPromptInput,
                        onValueChange = { viewModel.updatePromptInput(it) },
                        placeholder = { Text("e.g. Build a high performance caching module in Kotlin with unit tests...", fontSize = 13.sp, color = OmniaTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .testTag("agent_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OmniaElectricBlue,
                            unfocusedBorderColor = OmniaSubtleBorder,
                            focusedContainerColor = OmniaElevatedSurface,
                            unfocusedContainerColor = OmniaElevatedSurface,
                            focusedTextColor = OmniaTextPrimary,
                            unfocusedTextColor = OmniaTextPrimary,
                            cursorColor = OmniaNeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (uiState.isEngineRunning) {
                            Button(
                                onClick = { viewModel.cancelPipeline() },
                                colors = ButtonDefaults.buttonColors(containerColor = OmniaErrorRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CANCEL PIPELINE", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.executeAgentPipeline() },
                                enabled = uiState.userPromptInput.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OmniaElectricBlue,
                                    disabledContainerColor = OmniaElevatedSurface
                                ),
                                modifier = Modifier.testTag("execute_agents_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RUN 6-AGENT PIPELINE", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs (Active vs History)
            Surface(
                color = OmniaSecondaryBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        color = if (selectedTab == 0) OmniaElevatedSurface else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = if (selectedTab == 0) androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                "Active Pipeline",
                                color = if (selectedTab == 0) OmniaNeonCyan else OmniaTextSecondary,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        color = if (selectedTab == 1) OmniaElevatedSurface else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = if (selectedTab == 1) androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                "History (${uiState.taskHistory.size})",
                                color = if (selectedTab == 1) OmniaNeonCyan else OmniaTextSecondary,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                val activeRun = uiState.currentActiveRun
                if (activeRun == null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = OmniaCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = OmniaTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ready to Coordinate",
                                fontWeight = FontWeight.Bold,
                                color = OmniaTextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter a task above and click 'RUN 6-AGENT PIPELINE' to launch research, coding, testing, writing, security, and review coordinators.",
                                fontSize = 12.sp,
                                color = OmniaTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Quality Score Header Card if completed
                            if (activeRun.status == AgentRunStatus.COMPLETED) {
                                Surface(
                                    color = OmniaCardBg,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSuccessGreen.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = OmniaSuccessGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Pipeline Execution Complete",
                                                    fontWeight = FontWeight.Bold,
                                                    color = OmniaSuccessGreen,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "All 6 specialized agents finished successfully.",
                                                fontSize = 12.sp,
                                                color = OmniaTextSecondary
                                            )
                                        }
                                        Surface(
                                            color = OmniaElevatedSurface,
                                            shape = CircleShape,
                                            border = androidx.compose.foundation.BorderStroke(2.dp, OmniaSuccessGreen),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${activeRun.qualityScore}",
                                                    color = OmniaSuccessGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Agent Step Items
                        items(activeRun.steps, key = { it.agentType.name }) { step ->
                            AgentStepCard(step = step)
                        }
                    }
                }
            } else {
                // Task History List
                if (uiState.taskHistory.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = OmniaCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No execution history yet.",
                                color = OmniaTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.taskHistory, key = { it.id }) { historyRun ->
                            Surface(
                                onClick = {
                                    viewModel.selectHistoryRun(historyRun)
                                    selectedTab = 0
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = OmniaCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = historyRun.userPrompt,
                                            fontWeight = FontWeight.Bold,
                                            color = OmniaTextPrimary,
                                            maxLines = 1,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(OmniaElevatedSurface)
                                                .border(1.dp, OmniaNeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${historyRun.qualityScore}/100",
                                                color = OmniaBrightCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Status: ${historyRun.status.name} • ${historyRun.steps.size} Agents Executed",
                                        fontSize = 11.sp,
                                        color = OmniaTextSecondary
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

@Composable
fun AgentStepCard(step: AgentStepLog) {
    var expanded by remember { mutableStateOf(step.status == StepStatus.EXECUTING || step.status == StepStatus.THINKING) }

    val agentColor = when (step.agentType) {
        AgentType.RESEARCH -> AgentResearchColor
        AgentType.CODING -> AgentCodingColor
        AgentType.TESTING -> AgentTestingColor
        AgentType.WRITING -> AgentWritingColor
        AgentType.SECURITY -> AgentSecurityColor
        AgentType.REVIEW -> AgentReviewColor
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = OmniaCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (step.status == StepStatus.EXECUTING || step.status == StepStatus.THINKING) agentColor.copy(alpha = 0.6f)
            else OmniaSubtleBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(agentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = step.agentType.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OmniaTextPrimary
                        )
                        Text(
                            text = step.thoughtLog,
                            fontSize = 11.sp,
                            color = OmniaTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (step.status) {
                        StepStatus.PENDING -> Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OmniaElevatedSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PENDING", fontSize = 10.sp, color = OmniaTextMuted, fontWeight = FontWeight.Bold)
                        }
                        StepStatus.THINKING, StepStatus.EXECUTING -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = agentColor
                        )
                        StepStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OmniaSuccessGreen, modifier = Modifier.size(18.dp))
                        StepStatus.FAILED -> Icon(Icons.Default.Error, contentDescription = null, tint = OmniaErrorRed, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = OmniaTextSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded && step.outputResult.isNotBlank()) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = OmniaSubtleBorder.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
                    MarkdownText(text = step.outputResult)
                }
            }
        }
    }
}
