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
import com.example.ui.theme.*

@Composable
fun ModelSelectorHeader(
    activeProvider: ProviderConfig,
    selectedModelId: String,
    availableModels: List<AIModel>,
    isHighThinking: Boolean,
    onModelSelected: (String) -> Unit,
    onThinkingToggled: (Boolean) -> Unit,
    onSystemPromptClicked: () -> Unit,
    onRefreshModels: () -> Unit = {},
    onManualModelAdded: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dialogOpen by remember { mutableStateOf(false) }

    val activeModel = availableModels.firstOrNull { it.id == selectedModelId }
        ?: AIModel(selectedModelId, selectedModelId, activeProvider.type)

    Surface(
        color = OmniaMainBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Model Selector Dropdown Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(OmniaCardBg)
                    .border(1.dp, OmniaElectricViolet.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable { dialogOpen = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("model_selector_dropdown"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (activeProvider.enabled) OmniaSuccessGreen else Color(0xFFFF5252))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeModel.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OmniaTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = activeModel.id,
                        fontSize = 10.sp,
                        color = OmniaElectricViolet
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Model",
                    tint = OmniaElectricViolet
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // High Thinking Mode Toggle Pill
                FilterChip(
                    selected = isHighThinking,
                    onClick = { onThinkingToggled(!isHighThinking) },
                    label = { Text("Reasoning", fontSize = 11.sp, color = if (isHighThinking) OmniaLavenderHighlight else OmniaTextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Reasoning Level",
                            tint = if (isHighThinking) OmniaAccentCyan else OmniaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("thinking_mode_chip"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OmniaElevatedSurface,
                        containerColor = OmniaCardBg
                    )
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
                        tint = OmniaElectricViolet
                    )
                }
            }
        }
    }

    if (dialogOpen) {
        ModelSelectorDialog(
            activeProvider = activeProvider,
            selectedModelId = selectedModelId,
            availableModels = availableModels,
            isLoading = false,
            onModelSelected = onModelSelected,
            onRefreshModels = onRefreshModels,
            onManualModelAdded = onManualModelAdded,
            onDismiss = { dialogOpen = false }
        )
    }
}
