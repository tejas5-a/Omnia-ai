package com.example.ui.screens.workflows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.model.StepStatus
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowScreen(
    viewModel: WorkflowViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeWf = uiState.activeWorkflow

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
                                    Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Visual Workflow Studio",
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
            if (activeWf != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OmniaCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(OmniaNeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeWf.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OmniaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeWf.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = OmniaTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.runWorkflow() },
                            enabled = !uiState.isRunning,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OmniaElectricBlue,
                                disabledContainerColor = OmniaElevatedSurface
                            ),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("run_workflow_button")
                        ) {
                            if (uiState.isRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EXECUTING WORKFLOW...", fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EXECUTE WORKFLOW", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Workflow Step Sequence",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val displayNodes = uiState.activeRunLog?.nodesState ?: activeWf.nodes

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayNodes, key = { it.id }) { node ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OmniaCardBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (node.status) {
                                    StepStatus.EXECUTING -> OmniaNeonCyan.copy(alpha = 0.6f)
                                    StepStatus.COMPLETED -> OmniaSuccessGreen.copy(alpha = 0.4f)
                                    else -> OmniaSubtleBorder
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = node.title,
                                        fontWeight = FontWeight.Bold,
                                        color = OmniaTextPrimary,
                                        fontSize = 14.sp
                                    )
                                    when (node.status) {
                                        StepStatus.PENDING -> Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(OmniaElevatedSurface)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("PENDING", fontSize = 10.sp, color = OmniaTextMuted, fontWeight = FontWeight.Bold)
                                        }
                                        StepStatus.EXECUTING -> CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = OmniaNeonCyan
                                        )
                                        StepStatus.COMPLETED -> Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OmniaSuccessGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("COMPLETED", color = OmniaSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        else -> {}
                                    }
                                }

                                if (node.outputResult.isNotBlank()) {
                                    HorizontalDivider(
                                        color = OmniaSubtleBorder.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    MarkdownText(text = node.outputResult)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
