package com.flowerlauncher.model

import kotlinx.serialization.Serializable

@Serializable
enum class HomeLayoutStyle {
    FLOWER_ARC, // Signature circular arc flower layout
    FLUID_GRID  // Freeform floating sub-grid layout
}

@Serializable
data class HomeItemPosition(
    val id: String,
    val packageName: String,
    val className: String,
    val label: String,
    // Sub-grid floating coordinates (REAL in reference APK Room DB)
    val cellX: Float = 0f,
    val cellY: Float = 0f,
    val spanX: Float = 1f,
    val spanY: Float = 1f,
    val zIndex: Int = 0,
    // Secondary action (Pro parity: double-tap shortcut)
    val secondaryPackageName: String? = null,
    val secondaryClassName: String? = null
)

@Serializable
enum class GestureType {
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    DOUBLE_TAP_EMPTY,
    PINCH_IN
}

@Serializable
enum class GestureAction {
    OPEN_DRAWER,
    OPEN_SEARCH,
    OPEN_NOTIFICATIONS,
    OPEN_WIDGETS,
    LOCK_SCREEN,
    OPEN_SETTINGS,
    NONE
}
