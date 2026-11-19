package com.flowerlauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.flowerlauncher.data.AppDiscoveryRepository
import com.flowerlauncher.model.AppInfo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlowerArcLayout(
    apps: List<AppInfo>,
    repository: AppDiscoveryRepository,
    modifier: Modifier = Modifier,
    radiusDp: Float = 135f,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit = {},
    onAppDoubleTap: (AppInfo) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = configuration.screenWidthDp.dp
    val radius = with(density) { radiusDp.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((radiusDp * 2 + 100).dp),
        contentAlignment = Alignment.Center
    ) {
        val totalApps = apps.size
        if (totalApps == 0) return@Box

        // Angle distribution: If <= 6 apps, spread over an arc or full circle
        val startAngleRad = if (totalApps <= 6) {
            -Math.PI / 2.0 // Start at top
        } else {
            0.0
        }
        val angleStep = (2.0 * Math.PI) / totalApps.toDouble()

        apps.forEachIndexed { index, app ->
            val angle = startAngleRad + (index * angleStep)
            val offsetX = (radius * cos(angle)).toInt()
            val offsetY = (radius * sin(angle)).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
            ) {
                AppIconItem(
                    app = app,
                    iconDrawable = repository.getAppIcon(app.packageName, app.className),
                    iconSize = 58.dp,
                    showLabel = true,
                    onAppClick = { onAppClick(app) },
                    onAppLongClick = { onAppLongClick(app) },
                    onAppDoubleTap = { onAppDoubleTap(app) }
                )
            }
        }
    }
}
