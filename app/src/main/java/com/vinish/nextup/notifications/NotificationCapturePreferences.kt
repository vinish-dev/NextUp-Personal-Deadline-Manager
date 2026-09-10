package com.vinish.nextup.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat

object NotificationCapturePreferences {

    private const val PREFS_NAME = "nextup_smart_capture_prefs"
    private const val KEY_ENABLED = "key_smart_capture_enabled"
    private const val KEY_AUTO_CREATE = "key_smart_capture_auto_create"

    fun isFeatureEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setFeatureEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun isAutoCreateEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_CREATE, false)
    }

    fun setAutoCreateEnabled(context: Context, autoCreate: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTO_CREATE, autoCreate)
            .apply()
    }

    fun hasNotificationListenerPermission(context: Context): Boolean {
        val packageName = context.packageName
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return listeners.contains(packageName)
    }
}
