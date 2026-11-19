package com.flowerlauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.flowerlauncher.model.AppInfo
import com.flowerlauncher.model.GestureType
import com.flowerlauncher.model.HomeLayoutStyle
import com.flowerlauncher.ui.FlowerLauncherViewModel
import com.flowerlauncher.ui.LauncherScreen
import com.flowerlauncher.ui.components.AppIconItem
import com.flowerlauncher.ui.components.FlowerArcLayout
import com.flowerlauncher.ui.components.SmartClockWidget
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlowerHomeScreen(
    viewModel: FlowerLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val flowerArcApps by viewModel.flowerArcApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val homeLayoutStyle by viewModel.homeLayoutStyle.collectAsState()

    var showHomeContextMenu by remember { mutableStateOf(false) }
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }

    // Multi-Touch Gesture Detection
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.executeGesture(GestureType.DOUBLE_TAP_EMPTY)
                    },
                    onLongPress = {
                        showHomeContextMenu = true
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        val threshold = 120f
                        if (abs(totalDragY) > abs(totalDragX)) {
                            if (totalDragY < -threshold) {
                                viewModel.executeGesture(GestureType.SWIPE_UP)
                            } else if (totalDragY > threshold) {
                                viewModel.executeGesture(GestureType.SWIPE_DOWN)
                            }
                        } else {
                            if (totalDragX < -threshold) {
                                viewModel.executeGesture(GestureType.SWIPE_LEFT)
                            } else if (totalDragX > threshold) {
                                viewModel.executeGesture(GestureType.SWIPE_RIGHT)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Smart Clock & Battery
            SmartClockWidget(
                onClockClick = { viewModel.navigateTo(LauncherScreen.SEARCH) },
                onDateClick = { viewModel.navigateTo(LauncherScreen.WIDGETS) }
            )

            // 2. Middle/Bottom Layout (Flower Arc vs Grid)
            if (homeLayoutStyle == HomeLayoutStyle.FLOWER_ARC) {
                FlowerArcLayout(
                    apps = flowerArcApps,
                    repository = viewModel.repository,
                    onAppClick = { app -> viewModel.launchApp(app) },
                    onAppLongClick = { app -> selectedAppForMenu = app },
                    onAppDoubleTap = { app ->
                        // Pro Parity: Secondary action on double-tap
                        viewModel.launchApp(app)
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(flowerArcApps) { app ->
                        AppIconItem(
                            app = app,
                            iconDrawable = viewModel.repository.getAppIcon(app.packageName, app.className),
                            onAppClick = { viewModel.launchApp(app) },
                            onAppLongClick = { selectedAppForMenu = app }
                        )
                    }
                }
            }

            // 3. Bottom Dock / Quick Drawer Pull Indicator
            IconButton(
                onClick = { viewModel.navigateTo(LauncherScreen.DRAWER) },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(22.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Open Drawer",
                    tint = Color.White
                )
            }
        }

        // Long Press App Options Bottom Sheet / Dialog
        selectedAppForMenu?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                title = { Text(text = app.label) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                selectedAppForMenu = null
                                viewModel.openAppInfo(app.packageName)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("App Details / Settings")
                        }

                        TextButton(
                            onClick = {
                                selectedAppForMenu = null
                                viewModel.toggleAppHidden(app.packageName)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Hide App (Privacy Vault)")
                        }

                        TextButton(
                            onClick = {
                                selectedAppForMenu = null
                                viewModel.uninstallApp(app.packageName)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Uninstall App", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedAppForMenu = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // Home Long Press Menu
        if (showHomeContextMenu) {
            AlertDialog(
                onDismissRequest = { showHomeContextMenu = false },
                title = { Text("Home Screen Options") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showHomeContextMenu = false
                                val nextStyle = if (homeLayoutStyle == HomeLayoutStyle.FLOWER_ARC) {
                                    HomeLayoutStyle.FLUID_GRID
                                } else {
                                    HomeLayoutStyle.FLOWER_ARC
                                }
                                viewModel.setHomeLayoutStyle(nextStyle)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (homeLayoutStyle == HomeLayoutStyle.FLOWER_ARC) "Switch to Grid Layout" else "Switch to Flower Layout")
                        }

                        TextButton(
                            onClick = {
                                showHomeContextMenu = false
                                viewModel.navigateTo(LauncherScreen.SETTINGS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Launcher Settings")
                        }

                        TextButton(
                            onClick = {
                                showHomeContextMenu = false
                                viewModel.navigateTo(LauncherScreen.WIDGETS)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Widgets Page")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHomeContextMenu = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
