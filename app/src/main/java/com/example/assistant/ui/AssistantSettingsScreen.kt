package com.example.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.assistant.service.MaxWakeWordService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isWakeWordEnabled by remember { mutableStateOf(false) }
    var isTtsAudioEnabled by remember { mutableStateOf(true) }
    var speechRate by remember { mutableStateOf(1.0f) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MAX Assistant Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wake Word Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "'Hey Max' Wake Word Detection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Run background listener for hands-free continuous activation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isWakeWordEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !hasMicPermission) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    isWakeWordEnabled = enabled
                                    if (enabled) {
                                        MaxWakeWordService.startService(context)
                                    } else {
                                        MaxWakeWordService.stopService(context)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Voice Feedback
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Speech & Audio Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spoken Audio Responses (TTS)")
                        Switch(
                            checked = isTtsAudioEnabled,
                            onCheckedChange = { isTtsAudioEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Speech Speed: ${"%.1f".format(speechRate)}x", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.5f..2.0f,
                        steps = 5
                    )
                }
            }

            // Permissions Card
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionRow(
                        icon = Icons.Default.Mic,
                        title = "Microphone Access",
                        isGranted = hasMicPermission,
                        onRequest = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionRow(
                        icon = Icons.Default.People,
                        title = "Contacts Access",
                        isGranted = hasContactsPermission,
                        onRequest = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
                    )
                }
            }

            // Android System Limitations Explanation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android System & Privacy Policies",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Screen-off Listening: Android strictly restricts continuous background microphone access during screen-off states to protect user privacy and battery life.\n" +
                                "• Foreground Notification: When 'Hey Max' continuous wake-word detection is turned on, Android displays an active microphone notification badge.\n" +
                                "• Messaging Safety: External messaging apps (WhatsApp, Instagram, Messenger) require explicitly pre-filled message review before sending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        if (isGranted) {
            Text("Granted", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        } else {
            Button(onClick = onRequest, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Grant")
            }
        }
    }
}
