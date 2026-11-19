package com.flowerlauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlowerNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts = _notificationCounts.asStateFlow()

        var isConnected: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        updateCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        updateCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        updateCounts()
    }

    private fun updateCounts() {
        try {
            val active = activeNotifications ?: return
            val counts = mutableMapOf<String, Int>()
            for (sbn in active) {
                if (!sbn.isOngoing) {
                    val pkg = sbn.packageName
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
            _notificationCounts.value = counts
        } catch (_: Exception) {}
    }
}
