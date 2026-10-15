package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
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
import com.example.data.model.AIModel
import com.example.data.model.ProviderConfig

@Composable
fun ModelSelectorHeader(
    activeProvider: ProviderConfig,
    selectedModelId: String,
    availableModels: List<AIModel>,
    isHighThinking: Boolean,
    onModelSelected: (String) -> Unit,
    onThinkingToggled: (Boolean) -> Unit,
    onSystemPromptClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val activeModel = availableModels.firstOrNull { it.id == selectedModelId }
        ?: AIModel(selectedModelId, selectedModelId, activeProvider.type)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Model Selector Dropdown Pill
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("model_selector_dropdown"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (activeProvider.enabled) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeModel.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Model",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    Text(
                        text = "${activeProvider.name} Models",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()

                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = model.name,
                                            fontWeight = if (model.id == selectedModelId) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (model.isFreeTier) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "FREE",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (model.description.isNotBlank()) {
                                        Text(
                                            text = model.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onModelSelected(model.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // High Thinking Mode Toggle Pill
                FilterChip(
                    selected = isHighThinking,
                    onClick = { onThinkingToggled(!isHighThinking) },
                    label = {
                        Text(
                            text = "Thinking",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Thinking Level",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("thinking_mode_chip")
                )

                Spacer(modifier = Modifier.width(6.dp))

                // System Instruction Button
                IconButton(
                    onClick = onSystemPromptClicked,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("system_instruction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "System Instructions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
