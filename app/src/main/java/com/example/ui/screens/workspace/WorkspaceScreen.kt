package com.example.ui.screens.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeBlockView
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Coding Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // File Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.files.forEach { file ->
                    val isSelected = file.path == uiState.activeFile?.path
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectFile(file) },
                        label = { Text(file.name) },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )
                }
            }

            // Prompt Bar for Code Refactoring / Debugging
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.promptInput,
                        onValueChange = { viewModel.updatePrompt(it) },
                        placeholder = { Text("AI Refactor request (e.g. Add coroutine error handling...)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("workspace_refactor_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.generateCodeFix() },
                        enabled = !uiState.isGenerating && uiState.promptInput.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("refactor_code_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Code Display Container
            uiState.activeFile?.let { file ->
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "Current Code (${file.path})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        CodeBlockView(code = file.content, language = file.language)
                    }

                    if (uiState.diffOutput != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AI Refactored Code & Analysis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            MarkdownText(text = uiState.diffOutput!!)
                        }
                    }
                }
            }
        }
    }
}
