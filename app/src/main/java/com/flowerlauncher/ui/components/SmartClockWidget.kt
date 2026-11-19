package com.flowerlauncher.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmartClockWidget(
    modifier: Modifier = Modifier,
    onClockClick: () -> Unit = {},
    onDateClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    // Live Clock Ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMMM d", Locale.getDefault()) }

    // Battery Status
    val batteryPercent = remember(currentTime) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 100
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Time
        Text(
            text = timeFormat.format(currentTime),
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            letterSpacing = (-2).sp,
            modifier = Modifier.clickable { onClockClick() }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Date and Battery Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Date",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${dayFormat.format(currentTime)}, ${dateFormat.format(currentTime)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$batteryPercent%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
