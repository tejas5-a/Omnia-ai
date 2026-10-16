package com.example.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attachment
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.MessageStatus
import com.example.ui.components.AiOrbView
import com.example.ui.components.MarkdownText
import com.example.ui.components.ModelSelectorHeader
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    val attachedImages = remember { mutableStateListOf<Attachment>() }
    var systemInstructionText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.activeThread) {
        systemInstructionText = uiState.activeThread?.systemInstruction ?: ""
    }

    val isUserAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems <= 1) true
            else {
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleIndex >= totalItems - 2
            }
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.activeThread?.id) {
        if (uiState.messages.isNotEmpty() && isUserAtBottom) {
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }

    val lastMessageContent = uiState.messages.lastOrNull()?.content ?: ""
    LaunchedEffect(lastMessageContent) {
        if (isUserAtBottom && uiState.messages.isNotEmpty() && uiState.isGenerating) {
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                attachedImages.add(
                    Attachment(
                        type = "IMAGE",
                        uri = uri.toString(),
                        fileName = "photo.jpg",
                        mimeType = "image/jpeg",
                        base64Data = base64
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to attach photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.activeThread?.title ?: "OMNIA MAX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary
                        )
                        Text(
                            text = "Model: ${uiState.selectedModelId}",
                            fontSize = 11.sp,
                            color = OmniaElectricViolet
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("open_drawer_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = OmniaTextSecondary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.createNewThread() },
                        modifier = Modifier.testTag("appbar_new_chat_button")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = OmniaElectricViolet)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OmniaMainBg)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OmniaMainBg)
        ) {
            // Model Selector Bar
            uiState.activeProvider?.let { provider ->
                ModelSelectorHeader(
                    activeProvider = provider,
                    selectedModelId = uiState.selectedModelId,
                    availableModels = uiState.availableModels,
                    isHighThinking = uiState.isHighThinking,
                    onModelSelected = { viewModel.setSelectedModel(it) },
                    onThinkingToggled = { viewModel.toggleThinkingMode(it) },
                    onSystemPromptClicked = { viewModel.setSystemPromptSheetOpen(true) }
                )
            }

            // Message Feed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    EmptyChatWelcomeView(
                        activeModelId = uiState.selectedModelId,
                        onPromptSuggested = { prompt -> inputText = prompt }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatMessageBubble(
                                message = message,
                                onCopyClicked = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("AI Output", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onRegenerateClicked = { viewModel.regenerateResponse() }
                            )
                        }
                    }
                }
            }

            // Attached Images Row Preview
            if (attachedImages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachedImages.forEachIndexed { index, att ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OmniaElevatedSurface)
                        ) {
                            Text(
                                text = "📷",
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 24.sp
                            )
                            IconButton(
                                onClick = { attachedImages.removeAt(index) },
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Luminous Composer Input Container
            Surface(
                color = OmniaCardBg,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OmniaElectricViolet.copy(alpha = 0.25f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decorative Glowing Multi-State AI Orb
                    AiOrbView(
                        size = 36.dp,
                        isStreaming = uiState.isGenerating,
                        isThinking = uiState.isGenerating && uiState.messages.lastOrNull()?.content.isNullOrEmpty()
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("attach_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach File/Image",
                            tint = OmniaElectricViolet
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask OMNIA MAX anything...", fontSize = 14.sp, color = OmniaTextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OmniaPrimaryPurple,
                            unfocusedBorderColor = OmniaElectricViolet.copy(alpha = 0.3f),
                            focusedContainerColor = OmniaElevatedSurface,
                            unfocusedContainerColor = OmniaElevatedSurface,
                            focusedTextColor = OmniaTextPrimary,
                            unfocusedTextColor = OmniaTextPrimary
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AnimatedContent(
                        targetState = uiState.isGenerating,
                        transitionSpec = {
                            (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()) togetherWith
                                    (scaleOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + fadeOut())
                        },
                        label = "send_stop_button_morph"
                    ) { isGenerating ->
                        if (isGenerating) {
                            IconButton(
                                onClick = { viewModel.stopGeneration() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFF5252), CircleShape)
                                    .testTag("stop_generation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Generation",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() || attachedImages.isNotEmpty()) {
                                        val textToSend = inputText
                                        val attsToSend = attachedImages.toList()
                                        inputText = ""
                                        attachedImages.clear()
                                        viewModel.sendMessage(textToSend, attsToSend)
                                    }
                                },
                                enabled = inputText.isNotBlank() || attachedImages.isNotEmpty(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (inputText.isNotBlank() || attachedImages.isNotEmpty()) OmniaPrimaryPurple
                                        else OmniaElevatedSurface,
                                        CircleShape
                                    )
                                    .testTag("send_message_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Message",
                                    tint = if (inputText.isNotBlank() || attachedImages.isNotEmpty()) Color.White
                                    else OmniaTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.systemPromptSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setSystemPromptSheetOpen(false) },
            containerColor = OmniaCardBg,
            contentColor = OmniaTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Custom System Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OmniaTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configure how OMNIA MAX behaves, responds, and formats output for this conversation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniaTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = systemInstructionText,
                    onValueChange = { systemInstructionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("system_instruction_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.setSystemPromptSheetOpen(false) }) {
                        Text("Cancel", color = OmniaTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.updateSystemInstruction(systemInstructionText) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OmniaPrimaryPurple)
                    ) {
                        Text("Save Instructions", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopyClicked: () -> Unit,
    onRegenerateClicked: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = if (isUser) "You" else (message.agentName ?: "OMNIA MAX"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OmniaTextSecondary
            )
        }

        Surface(
            color = if (isUser) OmniaElevatedSurface else OmniaCardBg,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(
                    1.dp,
                    if (isUser) OmniaPrimaryPurple.copy(alpha = 0.4f) else OmniaElectricViolet.copy(alpha = 0.2f),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.status == MessageStatus.STREAMING && message.content.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = OmniaPrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thinking & Generating...", fontSize = 13.sp, color = OmniaTextSecondary)
                    }
                } else {
                    MarkdownText(text = message.content)
                }

                if (message.status == MessageStatus.ERROR) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error: ${message.errorMessage ?: "Failed to generate response"}",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isUser && (message.status == MessageStatus.SUCCESS || message.content.isNotEmpty())) {
                    var isCopied by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                onCopyClicked()
                                isCopied = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1800)
                                    isCopied = false
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            AnimatedContent(
                                targetState = isCopied,
                                transitionSpec = {
                                    (scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()) togetherWith
                                            (scaleOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + fadeOut())
                                },
                                label = "msg_copy_anim"
                            ) { copied ->
                                if (copied) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Copied",
                                        modifier = Modifier.size(14.dp),
                                        tint = OmniaSuccessGreen
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy message",
                                        modifier = Modifier.size(14.dp),
                                        tint = OmniaTextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onRegenerateClicked,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Regenerate response",
                                modifier = Modifier.size(14.dp),
                                tint = OmniaTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatWelcomeView(
    activeModelId: String,
    onPromptSuggested: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AiOrbView(size = 80.dp, isPulsing = true)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to OMNIA MAX",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OmniaTextPrimary
        )

        Text(
            text = "Active Model: $activeModelId",
            style = MaterialTheme.typography.bodyMedium,
            color = OmniaElectricViolet
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Suggested Prompts",
            style = MaterialTheme.typography.labelLarge,
            color = OmniaLavenderHighlight,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val prompts = listOf(
            "Architect a high-performance Kotlin Android app with clean architecture.",
            "Explain quantum computing principles with code examples.",
            "Run a multi-agent security audit on my authentication logic."
        )

        prompts.forEach { prompt ->
            OutlinedCard(
                onClick = { onPromptSuggested(prompt) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = OmniaCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, OmniaElectricViolet.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = prompt,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = OmniaTextPrimary
                )
            }
        }
    }
}
