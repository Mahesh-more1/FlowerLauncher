package com.flowerlauncher.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
enum class DefaultCategory(val id: String, val title: String) {
    COMMUNICATION("communication", "Communication"),
    INTERNET("internet", "Internet"),
    GAMES("games", "Games"),
    MEDIA("media", "Media"),
    UTILITIES("utilities", "Utilities"),
    SETTINGS("settings", "Settings")
}

@Serializable
data class CategoryDefinition(
    val id: String,
    val title: String,
    val isCustom: Boolean = false,
    val iconName: String = "Folder",
    val order: Int = 0
)

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val categoryId: String = DefaultCategory.UTILITIES.id,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val installTime: Long = 0L,
    val notificationBadgeCount: Int = 0
) {
    val componentKey: String
        get() = "$packageName/$className"
}
