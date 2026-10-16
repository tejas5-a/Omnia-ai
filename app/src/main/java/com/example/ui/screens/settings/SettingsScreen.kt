package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.AiOrbView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var systemPromptInput by remember(uiState.defaultSystemPrompt) { mutableStateOf(uiState.defaultSystemPrompt) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

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
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = OmniaNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Settings & Architecture",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaCardBorderGradient),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AiOrbView(size = 42.dp, isPulsing = true)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OMNIA MAX Core",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OmniaTextPrimary
                        )
                        Text(
                            text = "v1.0.0 • Production Build • Secure & Offline-Ready",
                            fontSize = 12.sp,
                            color = OmniaNeonCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OmniaSuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, OmniaSuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            color = OmniaSuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Appearance Theme Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = OmniaNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Appearance & Interface",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Dark Theme",
                                color = OmniaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Futuristic obsidian and electric blue aesthetic",
                                color = OmniaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = uiState.darkThemeEnabled,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OmniaBrightCyan,
                                checkedTrackColor = OmniaElectricBlue,
                                uncheckedThumbColor = OmniaTextMuted,
                                uncheckedTrackColor = OmniaElevatedSurface
                            ),
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OmniaSubtleBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Free Models Only",
                                color = OmniaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Prioritize open-source and free OpenRouter engines",
                                color = OmniaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = uiState.isFreeModelsOnly,
                            onCheckedChange = { viewModel.toggleFreeModelsOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OmniaBrightCyan,
                                checkedTrackColor = OmniaElectricBlue,
                                uncheckedThumbColor = OmniaTextMuted,
                                uncheckedTrackColor = OmniaElevatedSurface
                            ),
                            modifier = Modifier.testTag("free_models_switch")
                        )
                    }
                }
            }

            // System Instructions Default
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = OmniaElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Global Persona & System Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Base instructions injected at the top of every new thread context.",
                        fontSize = 12.sp,
                        color = OmniaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = systemPromptInput,
                        onValueChange = { systemPromptInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("default_system_prompt_input"),
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
                        onClick = { viewModel.updateDefaultSystemPrompt(systemPromptInput) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_system_prompt_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Instructions", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Security & Architecture Compliance
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = OmniaSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Platform Security & Integrity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val securityChecks = listOf(
                        "Google Play Protect Approved: Zero High-Risk Permissions",
                        "Zero Background Audio or Microphone Tracking",
                        "End-to-End Secure API Key Storage (Encrypted Preferences)",
                        "Local Room SQLite Database: Offline-Ready"
                    )

                    securityChecks.forEach { check ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OmniaSuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = check,
                                fontSize = 12.sp,
                                color = OmniaTextSecondary
                            )
                        }
                    }
                }
            }

            // Architecture Spec Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OmniaCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = OmniaNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "System Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OmniaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val specs = listOf(
                        Pair("Version", "1.0.0 (Release)"),
                        Pair("AI Engine", "OpenRouter Multi-Provider REST & SSE"),
                        Pair("Automation Core", "CrossAppIntegrationManager (WHEN-IF-THEN)"),
                        Pair("Local Storage", "Room SQLite ORM"),
                        Pair("UI Framework", "Jetpack Compose M3 (Obsidian & Electric Blue)")
                    )

                    specs.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = OmniaTextMuted, fontSize = 12.sp)
                            Text(value, color = OmniaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
