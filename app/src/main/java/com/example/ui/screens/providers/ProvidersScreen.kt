package com.example.ui.screens.providers

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.ProviderConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    viewModel: ProvidersViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Provider & API Settings",
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary,
                        fontSize = 18.sp
                    )
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
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Free Models Only Filter Card
            Surface(
                color = OmniaCardBg,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Free Models Only Filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OmniaTextPrimary
                        )
                        Text(
                            text = "Restrict model routing to verified free-tier endpoints.",
                            fontSize = 12.sp,
                            color = OmniaTextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isFreeModelsOnly,
                        onCheckedChange = { viewModel.toggleFreeModelsOnly(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OmniaElectricBlue,
                            uncheckedThumbColor = OmniaTextMuted,
                            uncheckedTrackColor = OmniaElevatedSurface
                        ),
                        modifier = Modifier.testTag("free_models_only_switch")
                    )
                }
            }

            // Error Message Banner
            uiState.errorMessage?.let { err ->
                Surface(
                    color = OmniaErrorRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaErrorRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = OmniaErrorRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = OmniaErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Status Message Banner
            uiState.statusMessage?.let { msg ->
                Surface(
                    color = OmniaElevatedSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OmniaNeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = OmniaNeonCyan
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        isLoadingModels = uiState.loadingProviderId == provider.id,
                        isTesting = uiState.testingProviderId == provider.id,
                        onFetchModelsClicked = { viewModel.fetchModelsForProvider(provider) },
                        onTestConnectionClicked = { viewModel.testConnectionForProvider(provider) },
                        onModelSelected = { modelId -> viewModel.selectModelForProvider(provider, modelId) },
                        onEnabledToggled = { enabled -> viewModel.toggleProviderEnabled(provider, enabled) },
                        onEditClicked = { viewModel.openEditDialog(provider) },
                        onManualModelClicked = { viewModel.openManualModelDialog(provider) }
                    )
                }
            }
        }
    }

    // Edit Provider Credentials Dialog
    if (uiState.editDialogOpen && uiState.selectedProviderForEdit != null) {
        val provider = uiState.selectedProviderForEdit!!
        var nameText by remember { mutableStateOf(provider.name) }
        var apiKeyText by remember { mutableStateOf(provider.apiKey) }
        var baseUrlText by remember { mutableStateOf(provider.baseUrl) }

        AlertDialog(
            onDismissRequest = { viewModel.closeEditDialog() },
            containerColor = OmniaCardBg,
            title = {
                Text(
                    "Configure ${provider.name}",
                    color = OmniaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Provider Name", color = OmniaTextSecondary) },
                        colors = outlinedTextFieldPalette(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("Secret API Key", color = OmniaTextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = outlinedTextFieldPalette(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_api_key_input"),
                        placeholder = { Text("Enter API Key (Encrypted in Keystore)", color = OmniaTextMuted) }
                    )

                    OutlinedTextField(
                        value = baseUrlText,
                        onValueChange = { baseUrlText = it },
                        label = { Text("Base Endpoint URL", color = OmniaTextSecondary) },
                        colors = outlinedTextFieldPalette(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_base_url_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProviderDetails(
                            providerId = provider.id,
                            name = nameText,
                            apiKey = apiKeyText,
                            baseUrl = baseUrlText
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue)
                ) {
                    Text("Save Credentials", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEditDialog() }) {
                    Text("Cancel", color = OmniaTextSecondary)
                }
            }
        )
    }

    // Manual Model ID Entry Dialog
    if (uiState.manualModelDialogOpen && uiState.manualModelProvider != null) {
        val provider = uiState.manualModelProvider!!
        var customModelIdText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.closeManualModelDialog() },
            containerColor = OmniaCardBg,
            title = {
                Text("Add Manual Model ID", color = OmniaTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Enter any exact model identifier for ${provider.name} (letters, numbers, hyphens, periods, slashes):",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniaTextSecondary
                    )
                    OutlinedTextField(
                        value = customModelIdText,
                        onValueChange = { customModelIdText = it },
                        label = { Text("Exact Model ID", color = OmniaTextSecondary) },
                        placeholder = { Text("e.g. anthropic/claude-3.7-sonnet", color = OmniaTextMuted) },
                        singleLine = true,
                        colors = outlinedTextFieldPalette(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_model_id_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveManualModelId(provider, customModelIdText) },
                    enabled = customModelIdText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElectricBlue),
                    modifier = Modifier.testTag("save_manual_model_button")
                ) {
                    Text("Save & Set Active", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeManualModelDialog() }) {
                    Text("Cancel", color = OmniaTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProviderCard(
    provider: ProviderConfig,
    isLoadingModels: Boolean,
    isTesting: Boolean,
    onFetchModelsClicked: () -> Unit,
    onTestConnectionClicked: () -> Unit,
    onModelSelected: (String) -> Unit,
    onEnabledToggled: (Boolean) -> Unit,
    onEditClicked: () -> Unit,
    onManualModelClicked: () -> Unit
) {
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var editableModelId by remember(provider.selectedModel) { mutableStateOf(provider.selectedModel) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = OmniaCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (provider.enabled) OmniaElectricBlue.copy(alpha = 0.5f) else OmniaSubtleBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Status Indicator, Name, Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (provider.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> OmniaSuccessGreen
                                    ConnectionStatus.ERROR -> OmniaErrorRed
                                    ConnectionStatus.CHECKING -> OmniaWarningAmber
                                    ConnectionStatus.UNKNOWN -> OmniaTextMuted
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                }

                Switch(
                    checked = provider.enabled,
                    onCheckedChange = onEnabledToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OmniaElectricBlue,
                        uncheckedThumbColor = OmniaTextMuted,
                        uncheckedTrackColor = OmniaElevatedSurface
                    ),
                    modifier = Modifier.testTag("provider_switch_${provider.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Endpoint: ${provider.baseUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = OmniaTextSecondary,
                fontSize = 11.sp
            )

            Text(
                text = "Key: " + if (provider.apiKey.isNotBlank()) "••••••••" + provider.apiKey.takeLast(4) else "Not configured",
                style = MaterialTheme.typography.bodySmall,
                color = if (provider.apiKey.isNotBlank()) OmniaNeonCyan else OmniaTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Fetch Models, Test Connection, Edit, Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fetch Models Button
                Button(
                    onClick = onFetchModelsClicked,
                    enabled = !isLoadingModels && !isTesting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElevatedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaElectricBlue.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("fetch_models_button_${provider.id}")
                ) {
                    if (isLoadingModels) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = OmniaNeonCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FETCHING", fontSize = 11.sp, color = OmniaNeonCyan)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp), tint = OmniaNeonCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FETCH (${provider.fetchedModels.size})", fontSize = 11.sp, color = OmniaTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // Test Connection Button
                Button(
                    onClick = onTestConnectionClicked,
                    enabled = !isTesting && !isLoadingModels,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniaElevatedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaNeonCyan.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("test_conn_button_${provider.id}")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = OmniaWarningAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TESTING", fontSize = 11.sp, color = OmniaWarningAmber)
                    } else {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp), tint = OmniaBrightCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST", fontSize = 11.sp, color = OmniaTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit Button
                OutlinedButton(
                    onClick = onEditClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = OmniaElevatedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("edit_provider_button_${provider.id}"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Credentials", modifier = Modifier.size(16.dp), tint = OmniaTextSecondary)
                }

                // Add Manual Model ID Button
                OutlinedButton(
                    onClick = onManualModelClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = OmniaElevatedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OmniaSubtleBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("manual_id_button_${provider.id}"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Model ID", modifier = Modifier.size(16.dp), tint = OmniaNeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FULLY EDITABLE MODEL ID INPUT FIELD
            Column {
                Text(
                    text = "Active Model Identifier",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OmniaTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = editableModelId,
                            onValueChange = { editableModelId = it },
                            placeholder = { Text("e.g. gemini-2.5-flash or model/name", color = OmniaTextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            colors = outlinedTextFieldPalette(),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                IconButton(onClick = { modelDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select from fetched", tint = OmniaNeonCyan)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_model_id_input_${provider.id}")
                        )

                        DropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false },
                            modifier = Modifier.background(OmniaElevatedSurface)
                        ) {
                            if (provider.fetchedModels.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No models fetched yet. Tap FETCH.", color = OmniaTextMuted, fontSize = 12.sp) },
                                    onClick = { modelDropdownExpanded = false }
                                )
                            } else {
                                provider.fetchedModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = model.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OmniaTextPrimary)
                                                Text(
                                                    text = model.id,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = OmniaNeonCyan
                                                )
                                            }
                                        },
                                        onClick = {
                                            editableModelId = model.id
                                            onModelSelected(model.id)
                                            modelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Apply / Save Button for Model ID
                    Button(
                        onClick = { onModelSelected(editableModelId.trim()) },
                        enabled = editableModelId.isNotBlank() && editableModelId != provider.selectedModel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OmniaElectricBlue,
                            disabledContainerColor = OmniaElevatedSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (editableModelId == provider.selectedModel) "ACTIVE" else "APPLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (editableModelId == provider.selectedModel) OmniaNeonCyan else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedTextFieldPalette() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OmniaElectricBlue,
    unfocusedBorderColor = OmniaSubtleBorder,
    focusedContainerColor = OmniaElevatedSurface,
    unfocusedContainerColor = OmniaElevatedSurface,
    focusedTextColor = OmniaTextPrimary,
    unfocusedTextColor = OmniaTextPrimary,
    cursorColor = OmniaNeonCyan
)
