package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIModel
import com.example.data.model.ProviderConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorDialog(
    activeProvider: ProviderConfig,
    selectedModelId: String,
    availableModels: List<AIModel>,
    isLoading: Boolean,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    onManualModelAdded: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
    var manualIdInput by remember { mutableStateOf("") }

    val filteredModels = availableModels.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OmniaCardBg,
        contentColor = OmniaTextPrimary,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title Header & Refresh Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select AI Model",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary
                    )
                    Text(
                        text = "Provider: ${activeProvider.name}",
                        fontSize = 12.sp,
                        color = OmniaElectricViolet
                    )
                }

                IconButton(
                    onClick = onRefreshModels,
                    enabled = !isLoading,
                    modifier = Modifier.testTag("refresh_models_icon")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = OmniaElectricViolet)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Models", tint = OmniaElectricViolet)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Filter Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search models or provider/model-id...", fontSize = 13.sp, color = OmniaTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OmniaTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OmniaPrimaryPurple,
                    unfocusedBorderColor = OmniaElectricViolet.copy(alpha = 0.3f),
                    focusedContainerColor = OmniaElevatedSurface,
                    unfocusedContainerColor = OmniaElevatedSurface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Manual Model ID Trigger Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showManualInput = !showManualInput }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showManualInput) "Hide Manual ID Entry" else "Enter Custom Model ID", color = OmniaAccentCyan, fontSize = 12.sp)
                }
            }

            if (showManualInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualIdInput,
                        onValueChange = { manualIdInput = it },
                        placeholder = { Text("e.g. openrouter/auto", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualIdInput.isNotBlank()) {
                                onManualModelAdded(manualIdInput.trim())
                                manualIdInput = ""
                                showManualInput = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Models List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .fillMaxWidth()
            ) {
                items(filteredModels, key = { it.id }) { model ->
                    val isSelected = model.id == selectedModelId

                    Surface(
                        color = if (isSelected) OmniaElevatedSurface else OmniaMainBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) OmniaPrimaryPurple else OmniaElectricViolet.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onModelSelected(model.id)
                                onDismiss()
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = model.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OmniaLavenderHighlight else OmniaTextPrimary,
                                    fontSize = 14.sp
                                )

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = OmniaSuccessGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Exact Model ID Displayed Separately
                            Text(
                                text = "Exact ID: ${model.id}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = OmniaElectricViolet
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Capability Indicators & Metadata Badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (model.isFreeTier) {
                                    BadgePill("FREE", OmniaSuccessGreen)
                                }
                                if (model.supportsThinking) {
                                    BadgePill("REASONING", OmniaAccentCyan)
                                }
                                if (model.supportsVision) {
                                    BadgePill("VISION", OmniaElectricViolet)
                                }
                                BadgePill("${model.contextLength / 1000}k Ctx", OmniaTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
