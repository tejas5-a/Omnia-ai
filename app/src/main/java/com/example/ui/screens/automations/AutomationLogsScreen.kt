package com.example.ui.screens.automations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationRunLog
import com.example.data.model.ExecutionStatus
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
                title = { Text("EXECUTION LOGS", fontWeight = FontWeight.Bold, color = Color(0xFF00F2FE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0E15))
            )
        },
        containerColor = Color(0xFF0D0E15)
    ) { padding ->
        if (runLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No execution history recorded yet.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(runLogs, key = { it.id }) { log ->
                    LogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: AutomationRunLog) {
    val statusColor = when (log.status) {
        ExecutionStatus.SUCCESS -> Color(0xFF4CAF50)
        ExecutionStatus.FAILED -> Color(0xFFFF5252)
        ExecutionStatus.SKIPPED -> Color(0xFFFF9800)
        ExecutionStatus.RUNNING -> Color(0xFF00F2FE)
    }

    val statusIcon = when (log.status) {
        ExecutionStatus.SUCCESS -> Icons.Default.CheckCircle
        ExecutionStatus.FAILED -> Icons.Default.Error
        else -> Icons.Default.Info
    }

    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF161824),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(log.automationName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        log.status.displayName,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(log.outputSummary, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Duration: ${log.durationMs} ms", fontSize = 10.sp, color = Color.Gray)
                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
            }

            if (log.stepResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Step Execution Breakdown:", fontSize = 11.sp, color = Color(0xFF00F2FE), fontWeight = FontWeight.Bold)
                for (step in log.stepResults) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(if (step.success) "✓" else "✗", color = if (step.success) Color(0xFF4CAF50) else Color(0xFFFF5252), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${step.stepName}: ${step.output}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
