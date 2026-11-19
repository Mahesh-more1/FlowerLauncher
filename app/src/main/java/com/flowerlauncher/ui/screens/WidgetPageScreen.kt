package com.flowerlauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowerlauncher.ui.FlowerLauncherViewModel
import com.flowerlauncher.ui.components.SmartClockWidget

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetPageScreen(
    viewModel: FlowerLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smart Widget Page",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Swipeable Widget Stack Carousel (Pro Feature Parity: Widget Stacks)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) { page ->
                when (page) {
                    0 -> ClockAndBatteryCard()
                    1 -> QuickNotesCard()
                    2 -> SystemStatusCard()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun ClockAndBatteryCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmartClockWidget()
        }
    }
}

@Composable
fun QuickNotesCard() {
    var noteText by remember { mutableStateOf("Welcome to Flower Launcher! Tap here to take notes.") }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Quick Sticky Note", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

@Composable
fun SystemStatusCard() {
    val runtime = Runtime.getRuntime()
    val totalMemory = runtime.totalMemory() / (1024 * 1024)
    val freeMemory = runtime.freeMemory() / (1024 * 1024)
    val usedMemory = totalMemory - freeMemory

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Device & Launcher Status", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            Text("Launcher Memory Usage: $usedMemory MB / $totalMemory MB", color = Color.White)
            Text("Processors: ${runtime.availableProcessors()} Cores", color = Color.White)
            Text("Target API: Android 16 (SDK 36)", color = Color.White)
            Text("Privacy Guarantee: 100% Offline, Zero Trackers", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
