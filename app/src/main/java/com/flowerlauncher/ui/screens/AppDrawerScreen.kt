package com.flowerlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowerlauncher.model.AppInfo
import com.flowerlauncher.model.CategoryDefinition
import com.flowerlauncher.model.DefaultCategory
import com.flowerlauncher.ui.FlowerLauncherViewModel
import com.flowerlauncher.ui.LauncherScreen
import com.flowerlauncher.ui.components.AppIconItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScreen(
    viewModel: FlowerLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allApps.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedAppForCategoryMove by remember { mutableStateOf<AppInfo?>(null) }

    // Filter apps by current category
    val filteredApps = remember(allApps, selectedCategoryId) {
        allApps.filter { !it.isHidden && it.categoryId == selectedCategoryId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
            // 1. Top Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { viewModel.navigateTo(LauncherScreen.SEARCH) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search apps, contacts, web...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }

            // 2. Main Content: Side Category Bar + Apps Grid
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Left Vertical Category Bar (Signature Smart Launcher navigation)
                LazyColumn(
                    modifier = Modifier
                        .width(68.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        val isSelected = cat.id == selectedCategoryId

                        IconButton(
                            onClick = { viewModel.selectCategory(cat.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat.id, cat.iconName),
                                contentDescription = cat.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Add Category Button (Pro Feature Parity: Create custom category!)
                    item {
                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Custom Category",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Apps Grid
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps in this category",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredApps) { app ->
                            AppIconItem(
                                app = app,
                                iconDrawable = viewModel.repository.getAppIcon(app.packageName, app.className),
                                onAppClick = { viewModel.launchApp(app) },
                                onAppLongClick = { selectedAppForCategoryMove = app }
                            )
                        }
                    }
                }
            }
        }

        // Add Custom Category Dialog
        if (showAddCategoryDialog) {
            var newCategoryTitle by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("New Custom Category") },
                text = {
                    Column {
                        Text("Create a custom drawer category tab (Pro Feature Unlocked):")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newCategoryTitle,
                            onValueChange = { newCategoryTitle = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryTitle.isNotBlank()) {
                                viewModel.addCustomCategory(newCategoryTitle.trim(), "Folder")
                                showAddCategoryDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Change App Category Dialog (Move app to another category)
        selectedAppForCategoryMove?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppForCategoryMove = null },
                title = { Text("Move \"${app.label}\" to...") },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                        items(categories.size) { idx ->
                            val cat = categories[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateAppCategory(app.packageName, cat.id)
                                        selectedAppForCategoryMove = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.id, cat.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(cat.title, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedAppForCategoryMove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun getCategoryIcon(id: String, iconName: String): ImageVector {
    return when (id) {
        DefaultCategory.COMMUNICATION.id -> Icons.Default.Chat
        DefaultCategory.INTERNET.id -> Icons.Default.Language
        DefaultCategory.GAMES.id -> Icons.Default.SportsEsports
        DefaultCategory.MEDIA.id -> Icons.Default.PlayCircle
        DefaultCategory.UTILITIES.id -> Icons.Default.Build
        DefaultCategory.SETTINGS.id -> Icons.Default.Settings
        else -> Icons.Default.Folder
    }
}
