package com.flowerlauncher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.flowerlauncher.ui.screens.*

class HomeScreenActivity : ComponentActivity() {

    private val viewModel: FlowerLauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Dynamic Theming
            val isDark = when (themeMode) {
                "light" -> false
                "dark", "amoled" -> true
                else -> isSystemInDarkTheme()
            }

            val colorScheme = if (isDark) {
                if (themeMode == "amoled") {
                    darkColorScheme(
                        background = Color.Black,
                        surface = Color.Black,
                        surfaceVariant = Color(0xFF161616),
                        primary = Color(0xFF64B5F6),
                        onPrimary = Color.Black
                    )
                } else {
                    darkColorScheme()
                }
            } else {
                lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    // System Back Handler
                    BackHandler(enabled = currentScreen != LauncherScreen.HOME) {
                        viewModel.navigateTo(LauncherScreen.HOME)
                    }

                    // Screen Transition Animated Switcher
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "LauncherScreens"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.HOME -> FlowerHomeScreen(viewModel = viewModel)
                            LauncherScreen.DRAWER -> AppDrawerScreen(viewModel = viewModel)
                            LauncherScreen.SEARCH -> SearchPanelScreen(viewModel = viewModel)
                            LauncherScreen.WIDGETS -> WidgetPageScreen(viewModel = viewModel)
                            LauncherScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            LauncherScreen.HIDDEN_VAULT -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
}
