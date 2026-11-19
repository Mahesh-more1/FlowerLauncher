package com.flowerlauncher.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowerlauncher.data.AppDiscoveryRepository
import com.flowerlauncher.data.LauncherPreferences
import com.flowerlauncher.model.*
import com.flowerlauncher.service.FlowerNotificationListenerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    DRAWER,
    SEARCH,
    WIDGETS,
    SETTINGS,
    HIDDEN_VAULT
}

class FlowerLauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    val repository = AppDiscoveryRepository(context)
    val preferences = LauncherPreferences(context)

    // Current Active Screen
    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    // All Installed Apps
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps = _allApps.asStateFlow()

    // Categories (Including custom categories - Pro parity)
    private val _categories = MutableStateFlow<List<CategoryDefinition>>(emptyList())
    val categories = _categories.asStateFlow()

    // Selected Drawer Category
    private val _selectedCategoryId = MutableStateFlow(DefaultCategory.COMMUNICATION.id)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    // Flower Arc Pinned Apps
    private val _flowerArcApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val flowerArcApps = _flowerArcApps.asStateFlow()

    // Search Query & Results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<AppInfo>> = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            apps.filter { !it.isHidden && it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme Mode
    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode = _themeMode.asStateFlow()

    // Home Layout Style
    private val _homeLayoutStyle = MutableStateFlow(preferences.homeLayoutStyle)
    val homeLayoutStyle = _homeLayoutStyle.asStateFlow()

    init {
        loadData()
        observeNotifications()
    }

    fun loadData() {
        viewModelScope.launch {
            val categoriesList = preferences.getCategories()
            _categories.value = categoriesList
            if (categoriesList.isNotEmpty() && categoriesList.none { it.id == _selectedCategoryId.value }) {
                _selectedCategoryId.value = categoriesList.first().id
            }

            val hidden = preferences.getHiddenPackages()
            val overrides = preferences.getCategoryOverrides()
            val apps = repository.discoverAllInstalledApps(hidden, overrides)
            _allApps.value = apps

            // Refresh Flower Arc Apps
            val savedArcKeys = preferences.getFlowerArcApps()
            val arcList = if (savedArcKeys.isNotEmpty()) {
                savedArcKeys.mapNotNull { key -> apps.find { it.componentKey == key } }
            } else {
                // Auto-pick top 6 common apps (Phone, Message, Chrome, Camera, Media, Settings)
                val defaults = listOf(
                    apps.find { it.categoryId == DefaultCategory.COMMUNICATION.id },
                    apps.find { it.categoryId == DefaultCategory.INTERNET.id },
                    apps.find { it.categoryId == DefaultCategory.MEDIA.id },
                    apps.find { it.categoryId == DefaultCategory.GAMES.id },
                    apps.find { it.categoryId == DefaultCategory.UTILITIES.id },
                    apps.find { it.categoryId == DefaultCategory.SETTINGS.id }
                ).filterNotNull().distinctBy { it.packageName }
                preferences.setFlowerArcApps(defaults.map { it.componentKey })
                defaults
            }
            _flowerArcApps.value = arcList
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            FlowerNotificationListenerService.notificationCounts.collect { counts ->
                _allApps.update { current ->
                    current.map { app ->
                        val count = counts[app.packageName] ?: 0
                        app.copy(notificationBadgeCount = count)
                    }
                }
                _flowerArcApps.update { current ->
                    current.map { app ->
                        val count = counts[app.packageName] ?: 0
                        app.copy(notificationBadgeCount = count)
                    }
                }
            }
        }
    }

    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open ${app.label}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun updateAppCategory(packageName: String, categoryId: String) {
        preferences.setCategoryOverride(packageName, categoryId)
        loadData()
    }

    fun toggleAppHidden(packageName: String) {
        val isHidden = preferences.getHiddenPackages().contains(packageName)
        preferences.setPackageHidden(packageName, !isHidden)
        loadData()
    }

    fun addCustomCategory(title: String, iconName: String) {
        val current = _categories.value.toMutableList()
        val newId = "custom_${System.currentTimeMillis()}"
        current.add(CategoryDefinition(id = newId, title = title, isCustom = true, iconName = iconName, order = current.size))
        preferences.saveCategories(current)
        _categories.value = current
        _selectedCategoryId.value = newId
    }

    fun deleteCategory(categoryId: String) {
        val current = _categories.value.filter { it.id != categoryId }
        preferences.saveCategories(current)
        _categories.value = current
        if (_selectedCategoryId.value == categoryId && current.isNotEmpty()) {
            _selectedCategoryId.value = current.first().id
        }
    }

    fun setThemeMode(mode: String) {
        preferences.themeMode = mode
        _themeMode.value = mode
    }

    fun setHomeLayoutStyle(style: HomeLayoutStyle) {
        preferences.homeLayoutStyle = style
        _homeLayoutStyle.value = style
    }

    fun updateFlowerArcApps(apps: List<AppInfo>) {
        _flowerArcApps.value = apps
        preferences.setFlowerArcApps(apps.map { it.componentKey })
    }

    fun executeGesture(gesture: GestureType) {
        when (preferences.getGestureAction(gesture)) {
            GestureAction.OPEN_DRAWER -> navigateTo(LauncherScreen.DRAWER)
            GestureAction.OPEN_SEARCH -> navigateTo(LauncherScreen.SEARCH)
            GestureAction.OPEN_WIDGETS -> navigateTo(LauncherScreen.WIDGETS)
            GestureAction.OPEN_SETTINGS -> navigateTo(LauncherScreen.SETTINGS)
            GestureAction.OPEN_NOTIFICATIONS -> expandStatusBar()
            GestureAction.LOCK_SCREEN -> lockScreen()
            GestureAction.NONE -> {}
        }
    }

    private fun expandStatusBar() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (_: Exception) {}
    }

    private fun lockScreen() {
        // Accessibility or device admin optional lock
        Toast.makeText(context, "Screen lock gesture recognized", Toast.LENGTH_SHORT).show()
    }
}
