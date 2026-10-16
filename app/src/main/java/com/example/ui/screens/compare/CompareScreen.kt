package com.example.ui.screens.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
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
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                                    Icons.Default.Compare,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Multi-Model Comparator",
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Side-by-Side Model Benchmarking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Benchmark outputs, latency, and quality across two distinct LLM architectures.",
                        fontSize = 12.sp,
                        color = OmniaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.modelA,
                            onValueChange = { viewModel.setModelA(it) },
                            label = { Text("Model Engine A", fontSize = 11.sp, color = OmniaNeonCyan) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OmniaNeonCyan,
                                unfocusedBorderColor = OmniaSubtleBorder,
                                focusedContainerColor = OmniaElevatedSurface,
                                unfocusedContainerColor = OmniaElevatedSurface,
                                focusedTextColor = OmniaTextPrimary,
                                unfocusedTextColor = OmniaTextPrimary,
                                cursorColor = OmniaNeonCyan
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.modelB,
                            onValueChange = { viewModel.setModelB(it) },
                            label = { Text("Model Engine B", fontSize = 11.sp, color = OmniaBrightCyan) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OmniaBrightCyan,
                                unfocusedBorderColor = OmniaSubtleBorder,
                                focusedContainerColor = OmniaElevatedSurface,
                                unfocusedContainerColor = OmniaElevatedSurface,
                                focusedTextColor = OmniaTextPrimary,
                                unfocusedTextColor = OmniaTextPrimary,
                                cursorColor = OmniaBrightCyan
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.promptInput,
                        onValueChange = { viewModel.updatePrompt(it) },
                        placeholder = { Text("Enter prompt to compare outputs simultaneously...", fontSize = 13.sp, color = OmniaTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .testTag("compare_prompt_input"),
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

                    Button(
                        onClick = { viewModel.runComparison() },
                        enabled = !uiState.isComparing && uiState.promptInput.isNotBlank(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("run_comparison_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OmniaElectricBlue,
                            disabledContainerColor = OmniaElevatedSurface
                        )
                    ) {
                        if (uiState.isComparing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BENCHMARKING...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN COMPARISON", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.comparisonResult?.let { result ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Model A Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = OmniaCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = result.modelA,
                                        fontWeight = FontWeight.Bold,
                                        color = OmniaNeonCyan,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(OmniaElevatedSurface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Latency: ${result.latencyA} ms",
                                            fontSize = 10.sp,
                                            color = OmniaTextSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    HorizontalDivider(
                                        color = OmniaSubtleBorder.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    MarkdownText(text = result.responseA)
                                }
                            }

                            // Model B Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = OmniaCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaBrightCyan.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = result.modelB,
                                        fontWeight = FontWeight.Bold,
                                        color = OmniaBrightCyan,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(OmniaElevatedSurface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Latency: ${result.latencyB} ms",
                                            fontSize = 10.sp,
                                            color = OmniaTextSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    HorizontalDivider(
                                        color = OmniaSubtleBorder.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    MarkdownText(text = result.responseB)
                                }
                            }
                        }
                    }

                    item {
                        // Reviewer Verdict
                        Surface(
                            color = OmniaCardBg,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaCardBorderGradient),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = OmniaElectricBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reviewer Verdict & Synthesis",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OmniaTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                MarkdownText(text = result.reviewerAnalysis)
                            }
                        }
                    }
                }
            }
        }
    }
}
