package com.example.ui.screens.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(
    viewModel: KnowledgeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Document Knowledge Center",
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Document", tint = OmniaNeonCyan)
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
            // Ask Question Box over Documents
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Semantic Search & Q&A across Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Query your private documents with vector-like synthesized context extraction.",
                        fontSize = 12.sp,
                        color = OmniaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.questionInput,
                        onValueChange = { viewModel.updateQuestion(it) },
                        placeholder = { Text("Ask a question about your indexed documents...", fontSize = 13.sp, color = OmniaTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .testTag("knowledge_qa_input"),
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
                        onClick = { viewModel.askQuestionOverKnowledge() },
                        enabled = !uiState.isSearching && uiState.documents.isNotEmpty(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("ask_knowledge_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OmniaElectricBlue,
                            disabledContainerColor = OmniaElevatedSurface
                        )
                    ) {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ANALYZING...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SEARCH & ANSWER", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.qaAnswer?.let { answer ->
                Surface(
                    color = OmniaCardBg,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = OmniaNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesized Answer:", fontWeight = FontWeight.Bold, color = OmniaNeonCyan, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        MarkdownText(text = answer)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Indexed Documents (${uiState.documents.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary
                )
                TextButton(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = OmniaNeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Document", color = OmniaNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.documents.isEmpty()) {
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
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = OmniaTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No documents indexed yet",
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add Document' above to ingest notes, API specs, or knowledge bases.",
                            color = OmniaTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.documents, key = { it.id }) { doc ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OmniaCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = OmniaElevatedSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = OmniaNeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.fileName,
                                        fontWeight = FontWeight.Bold,
                                        color = OmniaTextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tokens: ${doc.tokenCount} • Type: ${doc.fileType}",
                                        fontSize = 11.sp,
                                        color = OmniaTextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OmniaElevatedSurface)
                                        .border(1.dp, OmniaSuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "READY",
                                        color = OmniaSuccessGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var docName by remember { mutableStateOf("") }
        var docContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = OmniaCardBg,
            titleContentColor = OmniaTextPrimary,
            textContentColor = OmniaTextSecondary,
            title = {
                Text(
                    "Add Knowledge Document",
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("Document Title", fontSize = 12.sp, color = OmniaNeonCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OmniaNeonCyan,
                            unfocusedBorderColor = OmniaSubtleBorder,
                            focusedContainerColor = OmniaElevatedSurface,
                            unfocusedContainerColor = OmniaElevatedSurface,
                            focusedTextColor = OmniaTextPrimary,
                            unfocusedTextColor = OmniaTextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Document Content", fontSize = 12.sp, color = OmniaNeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OmniaNeonCyan,
                            unfocusedBorderColor = OmniaSubtleBorder,
                            focusedContainerColor = OmniaElevatedSurface,
                            unfocusedContainerColor = OmniaElevatedSurface,
                            focusedTextColor = OmniaTextPrimary,
                            unfocusedTextColor = OmniaTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docName.isNotBlank() && docContent.isNotBlank()) {
                            viewModel.addTextDocument(docName, docContent)
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue)
                ) {
                    Text("Add Document", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = OmniaTextSecondary)
                }
            }
        )
    }
}
