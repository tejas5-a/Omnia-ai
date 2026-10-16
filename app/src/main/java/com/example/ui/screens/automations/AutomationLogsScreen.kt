package com.example.ui.screens.automations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationRunLog
import com.example.data.model.ExecutionStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationLogsScreen(
    viewModel: AutomationsViewModel,
    onNavigateBack: () -> Unit
) {
    val runLogs by viewModel.runLogs.collectAsState()

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
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Execution Logs & History",
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
        containerColor = OmniaMainBg
    ) { padding ->
        if (runLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = OmniaTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No Execution History",
                            color = OmniaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Trigger an automation from the dashboard to inspect step results and telemetry here.",
                            color = OmniaTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(runLogs, key = { it.id }) { log ->
                    LogItemCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(log: AutomationRunLog) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }
    val timeString = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = OmniaCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (log.status) {
                ExecutionStatus.SUCCESS -> OmniaSuccessGreen.copy(alpha = 0.4f)
                ExecutionStatus.FAILED -> OmniaErrorRed.copy(alpha = 0.4f)
                ExecutionStatus.SKIPPED -> OmniaWarningAmber.copy(alpha = 0.4f)
                else -> OmniaSubtleBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    when (log.status) {
                        ExecutionStatus.SUCCESS -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OmniaSuccessGreen, modifier = Modifier.size(16.dp))
                        ExecutionStatus.FAILED -> Icon(Icons.Default.Error, contentDescription = null, tint = OmniaErrorRed, modifier = Modifier.size(16.dp))
                        ExecutionStatus.SKIPPED -> Icon(Icons.Default.Info, contentDescription = null, tint = OmniaWarningAmber, modifier = Modifier.size(16.dp))
                        else -> Icon(Icons.Default.Info, contentDescription = null, tint = OmniaNeonCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(log.automationName, color = OmniaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OmniaElevatedSurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        log.status.name,
                        color = when (log.status) {
                            ExecutionStatus.SUCCESS -> OmniaSuccessGreen
                            ExecutionStatus.FAILED -> OmniaErrorRed
                            ExecutionStatus.SKIPPED -> OmniaWarningAmber
                            else -> OmniaNeonCyan
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(timeString, color = OmniaTextMuted, fontSize = 11.sp)
                Text("Duration: ${log.durationMs}ms", color = OmniaTextSecondary, fontSize = 11.sp)
            }

            if (log.outputSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = OmniaElevatedSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        log.outputSummary,
                        color = OmniaTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (log.stepResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Executed Steps (${log.stepResults.size}):", color = OmniaNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                for (step in log.stepResults) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${step.stepName}", color = OmniaTextPrimary, fontSize = 11.sp)
                        Text(
                            step.output,
                            color = if (step.success) OmniaSuccessGreen else OmniaErrorRed,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
