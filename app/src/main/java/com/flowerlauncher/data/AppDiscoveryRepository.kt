package com.flowerlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import com.flowerlauncher.model.AppInfo
import com.flowerlauncher.model.DefaultCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppDiscoveryRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps

    // In-memory icon cache for smooth 60/120 FPS scrolling
    private val iconCache = ConcurrentHashMap<String, Drawable>()

    suspend fun discoverAllInstalledApps(
        hiddenPackages: Set<String> = emptySet(),
        categoryOverrides: Map<String, String> = emptyMap()
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        val ownPackage = context.packageName

        resolvedActivities.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val cls = resolveInfo.activityInfo.name
            if (pkg == ownPackage) return@mapNotNull null

            val label = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg
            }

            // Cache drawable
            val key = "$pkg/$cls"
            if (!iconCache.containsKey(key)) {
                try {
                    val icon = resolveInfo.loadIcon(packageManager)
                    iconCache[key] = icon
                } catch (_: Exception) {}
            }

            // Check categorization
            val assignedCategory = categoryOverrides[pkg] ?: determineCategory(resolveInfo.activityInfo.applicationInfo, pkg, label)

            val installTime = try {
                packageManager.getPackageInfo(pkg, 0).firstInstallTime
            } catch (e: Exception) {
                0L
            }

            AppInfo(
                packageName = pkg,
                className = cls,
                label = label,
                categoryId = assignedCategory,
                isHidden = hiddenPackages.contains(pkg),
                installTime = installTime
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun getAppIcon(packageName: String, className: String): Drawable? {
        val key = "$packageName/$className"
        iconCache[key]?.let { return it }
        return try {
            val icon = packageManager.getActivityIcon(android.content.ComponentName(packageName, className))
            iconCache[key] = icon
            icon
        } catch (e: Exception) {
            null
        }
    }

    private fun determineCategory(appInfo: ApplicationInfo, pkg: String, label: String): String {
        val lowerPkg = pkg.lowercase()
        val lowerLabel = label.lowercase()

        // 1. Android Official App Categories (API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return DefaultCategory.GAMES.id
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return DefaultCategory.MEDIA.id
                ApplicationInfo.CATEGORY_SOCIAL -> return DefaultCategory.COMMUNICATION.id
                ApplicationInfo.CATEGORY_MAPS,
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return DefaultCategory.UTILITIES.id
            }
        }

        // 2. Keyword & Heuristic Intent Classification
        if (lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("message")
            || lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("contact")
            || lowerPkg.contains("signal") || lowerPkg.contains("messenger") || lowerPkg.contains("mail")) {
            return DefaultCategory.COMMUNICATION.id
        }

        if (lowerPkg.contains("browser") || lowerPkg.contains("chrome") || lowerPkg.contains("firefox")
            || lowerPkg.contains("opera") || lowerPkg.contains("brave") || lowerPkg.contains("edge")
            || lowerPkg.contains("duckduckgo") || lowerPkg.contains("cloud") || lowerPkg.contains("drive")) {
            return DefaultCategory.INTERNET.id
        }

        if (lowerPkg.contains("game") || lowerPkg.contains("play.games") || lowerPkg.contains("puzzle")
            || lowerPkg.contains("chess") || lowerPkg.contains("arcade")) {
            return DefaultCategory.GAMES.id
        }

        if (lowerPkg.contains("music") || lowerPkg.contains("camera") || lowerPkg.contains("gallery")
            || lowerPkg.contains("photo") || lowerPkg.contains("video") || lowerPkg.contains("player")
            || lowerPkg.contains("youtube") || lowerPkg.contains("spotify") || lowerPkg.contains("netflix")
            || lowerPkg.contains("prime") || lowerPkg.contains("sound")) {
            return DefaultCategory.MEDIA.id
        }

        if (lowerPkg.contains("setting") || lowerPkg.contains("system") || lowerPkg.contains("config")) {
            return DefaultCategory.SETTINGS.id
        }

        return DefaultCategory.UTILITIES.id
    }
}
