package com.vinish.nextup

import android.app.Application
import com.vinish.nextup.data.DeadlineRepository
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.notification.DeadlineNotificationScheduler
import com.vinish.nextup.notification.NotificationHelper

class NextUpApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { DeadlineRepository(database.deadlineDao(), database.categoryDao()) }
    val notificationScheduler by lazy { DeadlineNotificationScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
