package com.example.ui.screens.automations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationTemplate
import com.example.data.model.AutomationTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateGalleryScreen(
    viewModel: AutomationsViewModel,
    onNavigateBack: () -> Unit
) {
    val templates = AutomationTemplates.ALL_TEMPLATES
    var installedIds by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TEMPLATE GALLERY", fontWeight = FontWeight.Bold, color = Color(0xFF00F2FE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0E15))
            )
        },
        containerColor = Color(0xFF0D0E15)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "One-Tap Cross-App Automations",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Select a pre-configured automation template to instantly install into your dashboard.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(templates, key = { it.id }) { template ->
                val isInstalled = installedIds.contains(template.id)
                TemplateCard(
                    template = template,
                    isInstalled = isInstalled,
                    onInstall = {
                        viewModel.installTemplate(template)
                        installedIds = installedIds + template.id
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: AutomationTemplate,
    isInstalled: Boolean,
    onInstall: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF161824),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00F2FE), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(template.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF212538)
                ) {
                    Text(
                        template.category,
                        color = Color(0xFF00F2FE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(template.description, color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Target: ${template.workflow.targetApp.displayName}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Button(
                    onClick = onInstall,
                    enabled = !isInstalled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInstalled) Color(0xFF212538) else Color(0xFF00F2FE),
                        disabledContainerColor = Color(0xFF212538)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isInstalled) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Installed", color = Color(0xFF4CAF50), fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install Template", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
