package com.flowerlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.flowerlauncher.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LauncherPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("flower_launcher_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    // Theme Mode: "system", "light", "dark", "amoled"
    var themeMode: String
        get() = prefs.getString("key_theme_mode", "amoled") ?: "amoled"
        set(value) = prefs.edit().putString("key_theme_mode", value).apply()

    // Home Layout Style: "flower_arc" or "fluid_grid"
    var homeLayoutStyle: HomeLayoutStyle
        get() {
            val name = prefs.getString("key_home_layout_style", HomeLayoutStyle.FLOWER_ARC.name)
            return try { HomeLayoutStyle.valueOf(name ?: HomeLayoutStyle.FLOWER_ARC.name) } catch (e: Exception) { HomeLayoutStyle.FLOWER_ARC }
        }
        set(value) = prefs.edit().putString("key_home_layout_style", value.name).apply()

    // Flower Arc Pinned App Keys (up to 8 in circle)
    fun getFlowerArcApps(): List<String> {
        val raw = prefs.getString("key_flower_arc_apps", null) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setFlowerArcApps(apps: List<String>) {
        prefs.edit().putString("key_flower_arc_apps", json.encodeToString(apps)).apply()
    }

    // Custom Categories (Pro Parity: create, edit, reorder, delete)
    fun getCategories(): List<CategoryDefinition> {
        val raw = prefs.getString("key_categories", null)
        if (raw != null) {
            try {
                return json.decodeFromString(raw)
            } catch (e: Exception) {}
        }
        // Defaults matching reference app
        return listOf(
            CategoryDefinition(DefaultCategory.COMMUNICATION.id, DefaultCategory.COMMUNICATION.title, iconName = "Chat", order = 0),
            CategoryDefinition(DefaultCategory.INTERNET.id, DefaultCategory.INTERNET.title, iconName = "Language", order = 1),
            CategoryDefinition(DefaultCategory.GAMES.id, DefaultCategory.GAMES.title, iconName = "SportsEsports", order = 2),
            CategoryDefinition(DefaultCategory.MEDIA.id, DefaultCategory.MEDIA.title, iconName = "PlayCircle", order = 3),
            CategoryDefinition(DefaultCategory.UTILITIES.id, DefaultCategory.UTILITIES.title, iconName = "Build", order = 4),
            CategoryDefinition(DefaultCategory.SETTINGS.id, DefaultCategory.SETTINGS.title, iconName = "Settings", order = 5)
        )
    }

    fun saveCategories(categories: List<CategoryDefinition>) {
        prefs.edit().putString("key_categories", json.encodeToString(categories)).apply()
    }

    // Category overrides per app package
    fun getCategoryOverrides(): Map<String, String> {
        val raw = prefs.getString("key_category_overrides", null) ?: return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setCategoryOverride(packageName: String, categoryId: String) {
        val current = getCategoryOverrides().toMutableMap()
        current[packageName] = categoryId
        prefs.edit().putString("key_category_overrides", json.encodeToString(current)).apply()
    }

    // Hidden Apps (Privacy Vault)
    fun getHiddenPackages(): Set<String> {
        return prefs.getStringSet("key_hidden_packages", emptySet()) ?: emptySet()
    }

    fun setPackageHidden(packageName: String, hidden: Boolean) {
        val current = getHiddenPackages().toMutableSet()
        if (hidden) current.add(packageName) else current.remove(packageName)
        prefs.edit().putStringSet("key_hidden_packages", current).apply()
    }

    // Gesture mappings
    fun getGestureAction(gesture: GestureType): GestureAction {
        val raw = prefs.getString("gesture_${gesture.name}", null) ?: return when (gesture) {
            GestureType.SWIPE_UP -> GestureAction.OPEN_DRAWER
            GestureType.SWIPE_DOWN -> GestureAction.OPEN_NOTIFICATIONS
            GestureType.SWIPE_LEFT -> GestureAction.OPEN_WIDGETS
            GestureType.SWIPE_RIGHT -> GestureAction.OPEN_SEARCH
            GestureType.DOUBLE_TAP_EMPTY -> GestureAction.LOCK_SCREEN
            GestureType.PINCH_IN -> GestureAction.OPEN_SETTINGS
        }
        return try { GestureAction.valueOf(raw) } catch (e: Exception) { GestureAction.NONE }
    }

    fun setGestureAction(gesture: GestureType, action: GestureAction) {
        prefs.edit().putString("gesture_${gesture.name}", action.name).apply()
    }

    // Export & Import Portable Backup (JSON)
    fun exportBackupJson(): String {
        val map = mapOf(
            "themeMode" to themeMode,
            "homeLayoutStyle" to homeLayoutStyle.name,
            "flowerArcApps" to json.encodeToString(getFlowerArcApps()),
            "categories" to json.encodeToString(getCategories()),
            "categoryOverrides" to json.encodeToString(getCategoryOverrides())
        )
        return json.encodeToString(map)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val map: Map<String, String> = json.decodeFromString(jsonString)
            map["themeMode"]?.let { themeMode = it }
            map["homeLayoutStyle"]?.let {
                try { homeLayoutStyle = HomeLayoutStyle.valueOf(it) } catch (e: Exception) {}
            }
            map["flowerArcApps"]?.let {
                try { setFlowerArcApps(json.decodeFromString(it)) } catch (e: Exception) {}
            }
            map["categories"]?.let {
                try { saveCategories(json.decodeFromString(it)) } catch (e: Exception) {}
            }
            map["categoryOverrides"]?.let {
                try {
                    val overrides: Map<String, String> = json.decodeFromString(it)
                    prefs.edit().putString("key_category_overrides", json.encodeToString(overrides)).apply()
                } catch (e: Exception) {}
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
