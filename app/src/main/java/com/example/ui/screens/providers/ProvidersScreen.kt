package com.example.ui.screens.providers

import androidx.compose.foundation.background
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
                title = { Text("API Provider Hub", fontWeight = FontWeight.Bold) },
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
            // Free Models Only Filter Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
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
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Restrict model routing to verified free-tier endpoints.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isFreeModelsOnly,
                        onCheckedChange = { viewModel.toggleFreeModelsOnly(it) },
                        modifier = Modifier.testTag("free_models_only_switch")
                    )
                }
            }

            // Error Message Banner
            uiState.errorMessage?.let { err ->
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Status Message Banner
            uiState.statusMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        isLoading = uiState.loadingProviderId == provider.id,
                        onFetchModelsClicked = { viewModel.fetchModelsForProvider(provider) },
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
            title = { Text("Configure ${provider.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Provider Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("Secret API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_api_key_input"),
                        placeholder = { Text("Enter API Key (Encrypted in Keystore)") }
                    )

                    OutlinedTextField(
                        value = baseUrlText,
                        onValueChange = { baseUrlText = it },
                        label = { Text("Base Endpoint URL") },
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
                    }
                ) {
                    Text("Save Credentials")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEditDialog() }) {
                    Text("Cancel")
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
            title = { Text("Add Manual Model ID") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the exact model identifier for ${provider.name} (e.g. provider/model-name):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customModelIdText,
                        onValueChange = { customModelIdText = it },
                        label = { Text("Exact Model ID") },
                        placeholder = { Text("e.g. openrouter/auto") },
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
                    modifier = Modifier.testTag("save_manual_model_button")
                ) {
                    Text("Save & Set Active")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeManualModelDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProviderCard(
    provider: ProviderConfig,
    isLoading: Boolean,
    onFetchModelsClicked: () -> Unit,
    onModelSelected: (String) -> Unit,
    onEnabledToggled: (Boolean) -> Unit,
    onEditClicked: () -> Unit,
    onManualModelClicked: () -> Unit
) {
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                                    ConnectionStatus.CONNECTED -> Color(0xFF10B981)
                                    ConnectionStatus.ERROR -> Color(0xFFEF4444)
                                    ConnectionStatus.CHECKING -> Color(0xFFF59E0B)
                                    ConnectionStatus.UNKNOWN -> Color.Gray
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = provider.enabled,
                    onCheckedChange = onEnabledToggled,
                    modifier = Modifier.testTag("provider_switch_${provider.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "URL: ${provider.baseUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Text(
                text = "Key: " + if (provider.apiKey.isNotBlank()) "••••••••" + provider.apiKey.takeLast(4) else "Not configured",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons: FETCH MODELS, EDIT, MANUAL ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFetchModelsClicked,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fetch_models_button_${provider.id}")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FETCHING...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FETCH (${provider.fetchedModels.size})", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onEditClicked,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("edit_provider_button_${provider.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                OutlinedButton(
                    onClick = onManualModelClicked,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("manual_id_button_${provider.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Manual ID", modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Model Picker
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = provider.selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Active Selected Model ID") },
                    trailingIcon = {
                        IconButton(onClick = { modelDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false }
                ) {
                    provider.fetchedModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = model.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "ID: ${model.id}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                onModelSelected(model.id)
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
