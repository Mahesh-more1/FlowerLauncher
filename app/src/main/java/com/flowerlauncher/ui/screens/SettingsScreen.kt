package com.flowerlauncher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowerlauncher.model.HomeLayoutStyle
import com.flowerlauncher.ui.FlowerLauncherViewModel
import com.flowerlauncher.ui.LauncherScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FlowerLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val themeMode by viewModel.themeMode.collectAsState()
    val homeLayoutStyle by viewModel.homeLayoutStyle.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    var showHiddenAppsDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(LauncherScreen.HOME) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Launcher Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Appearance
                item {
                    SettingsSectionHeader(title = "Appearance & Theming")
                }

                item {
                    SettingsCard {
                        // Theme Selector (Including AMOLED)
                        Text("Theme Mode", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("system" to "System", "light" to "Light", "dark" to "Dark", "amoled" to "AMOLED").forEach { (key, label) ->
                                val isSelected = themeMode == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemeMode(key) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                        // Home Layout Style Selector
                        Text("Home Screen Layout Style", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = homeLayoutStyle == HomeLayoutStyle.FLOWER_ARC,
                                onClick = { viewModel.setHomeLayoutStyle(HomeLayoutStyle.FLOWER_ARC) },
                                label = { Text("Flower (Circle Arc)") }
                            )
                            FilterChip(
                                selected = homeLayoutStyle == HomeLayoutStyle.FLUID_GRID,
                                onClick = { viewModel.setHomeLayoutStyle(HomeLayoutStyle.FLUID_GRID) },
                                label = { Text("Fluid Grid") }
                            )
                        }
                    }
                }

                // Section: Pro Parity Features
                item {
                    SettingsSectionHeader(title = "Pro Features (100% Unlocked)")
                }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHiddenAppsDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Privacy Vault & Hidden Apps", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Hide apps from drawer and search", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Drawer Categories", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("${categories.size} categories configured", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section: Backup & System Portability
                item {
                    SettingsSectionHeader(title = "Backup & Portability")
                }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val backupJson = viewModel.preferences.exportBackupJson()
                                    clipboardManager.setText(AnnotatedString(backupJson))
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export Backup (JSON)", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Export all layout settings and category definitions", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showImportDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Import Backup (JSON)", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Restore layout from a previously saved JSON configuration", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section: About & Privacy
                item {
                    SettingsSectionHeader(title = "Privacy & License")
                }

                item {
                    SettingsCard {
                        Text("Flower Launcher v1.0.0", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Clean-room independent reimplementation of Smart Launcher 6. Built with Kotlin & Jetpack Compose. Zero ads, zero trackers, 100% offline-first.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Hidden Apps Management Dialog
        if (showHiddenAppsDialog) {
            AlertDialog(
                onDismissRequest = { showHiddenAppsDialog = false },
                title = { Text("Privacy Vault (Hidden Apps)") },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                        items(allApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleAppHidden(app.packageName) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = app.isHidden,
                                    onCheckedChange = { viewModel.toggleAppHidden(app.packageName) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(app.label, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showHiddenAppsDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Import Backup Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Configuration JSON") },
                text = {
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Paste JSON here") },
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = viewModel.preferences.importBackupJson(importJsonText)
                            if (success) {
                                viewModel.loadData()
                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid backup JSON format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
