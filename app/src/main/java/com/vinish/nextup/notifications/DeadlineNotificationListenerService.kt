package com.vinish.nextup.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vinish.nextup.MainActivity
import com.vinish.nextup.R
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeadlineNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Deduplication cache: hash of (title + date) -> timestamp
    private val recentlyCaptured = HashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        // Do not process NextUp's own notifications or Android System UI ongoing alerts
        if (packageName == applicationContext.packageName) return
        if (packageName == "android" || packageName == "com.android.systemui") return

        val notification = sbn.notification ?: return
        // Ignore ongoing foreground services (media playback, timers, call status)
        val isOngoing = (notification.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0
        if (isOngoing) return

        if (!NotificationCapturePreferences.isFeatureEnabled(applicationContext)) return

        val extras: Bundle = notification.extras ?: return
        val rawTitle = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val rawText = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
        val contentText = if (!bigText.isNullOrBlank()) bigText else rawText

        // Parse through our deadline detection engine
        val parsed = NotificationDeadlineParser.parse(rawTitle, contentText, packageName) ?: return

        // Deduplication: prevent repeated triggers within 5 minutes for the same deadline title & date
        val dedupeKey = "${parsed.title.lowercase()}_${parsed.dueDate}_${parsed.dueTime}"
        val now = System.currentTimeMillis()
        val lastSeen = recentlyCaptured[dedupeKey] ?: 0L
        if (now - lastSeen < 5 * 60 * 1000) {
            return
        }
        recentlyCaptured[dedupeKey] = now

        // Check user setting: silent auto-create or prompt suggestion
        val isAutoCreate = NotificationCapturePreferences.isAutoCreateEnabled(applicationContext)

        if (isAutoCreate) {
            // Automatically insert into Room Database
            serviceScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val newDeadline = Deadline(
                        title = parsed.title,
                        description = parsed.description,
                        dueDate = parsed.dueDate,
                        dueTime = parsed.dueTime,
                        category = parsed.category,
                        priority = parsed.priority,
                        reminder = if (parsed.dueTime != null) Reminder.AT_TIME else Reminder.ONE_DAY_BEFORE,
                        isCompleted = false
                    )
                    val id = db.deadlineDao().insertDeadline(
                        com.vinish.nextup.data.local.DeadlineEntity.fromDomain(newDeadline)
                    )
                    val saved = newDeadline.copy(id = id)
                    DeadlineNotificationScheduler.scheduleReminder(applicationContext, saved)

                    // Notify user that task was automatically captured
                    showCapturedConfirmation(saved, packageName)
                } catch (_: Exception) {
                    // Ignore DB errors
                }
            }
        } else {
            // Present OTP-style action notification: "Add to NextUp" or tap to open
            showSuggestionNotification(parsed, packageName)
        }
    }

    private fun showSuggestionNotification(parsed: ParsedDeadlineInfo, sourcePackage: String) {
        val notificationId = ("suggest_" + parsed.title + parsed.dueDate).hashCode()
        val dueStr = DeadlineNotificationScheduler.formatDueInfo(parsed.dueDate, parsed.dueTime)

        // Intent to add immediately via broadcast
        val addIntent = Intent(this, DeadlineReminderReceiver::class.java).apply {
            action = DeadlineReminderReceiver.ACTION_AUTOFILL_ADD
            putExtra(DeadlineReminderReceiver.EXTRA_TITLE, parsed.title)
            putExtra(DeadlineReminderReceiver.EXTRA_DESCRIPTION, parsed.description)
            putExtra(DeadlineReminderReceiver.EXTRA_DUE_DATE, parsed.dueDate.toString())
            if (parsed.dueTime != null) {
                putExtra(DeadlineReminderReceiver.EXTRA_DUE_TIME, parsed.dueTime.toString())
            }
            putExtra(DeadlineReminderReceiver.EXTRA_CATEGORY, parsed.category.name)
            putExtra(DeadlineReminderReceiver.EXTRA_PRIORITY, parsed.priority.name)
            putExtra(DeadlineReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val addPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to dismiss suggestion
        val dismissIntent = Intent(this, DeadlineReminderReceiver::class.java).apply {
            action = DeadlineReminderReceiver.ACTION_DISMISS_SUGGESTION
            putExtra(DeadlineReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to open Add Screen with prefilled fields
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("autofill_title", parsed.title)
            putExtra("autofill_description", parsed.description)
            putExtra("autofill_date", parsed.dueDate.toString())
            if (parsed.dueTime != null) {
                putExtra("autofill_time", parsed.dueTime.toString())
            }
            putExtra("autofill_category", parsed.category.name)
            putExtra("autofill_priority", parsed.priority.name)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            notificationId + 2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DeadlineNotificationScheduler.SMART_CAPTURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF1E6BFF.toInt())
            .setContentTitle("Detected: ${parsed.title}")
            .setContentText("$dueStr · Tap to review or add")
            .setSubText("Smart Autofill")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Detected deadline from notification:\n• Task: ${parsed.title}\n• $dueStr\n• Category: ${parsed.category.displayName}"
                )
            )
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_check_action, "Add to NextUp", addPendingIntent)
            .addAction(R.drawable.ic_snooze_action, "Ignore", dismissPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    private fun showCapturedConfirmation(deadline: Deadline, sourcePackage: String) {
        val notificationId = ("auto_created_" + deadline.id).hashCode()
        val dueStr = DeadlineNotificationScheduler.formatDueInfo(deadline.dueDate, deadline.dueTime)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(DeadlineReminderReceiver.EXTRA_DEADLINE_ID, deadline.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DeadlineNotificationScheduler.SMART_CAPTURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF16A34A.toInt()) // Success Green
            .setContentTitle("Deadline Added: ${deadline.title}")
            .setContentText("$dueStr · Saved to NextUp")
            .setSubText("Auto-Captured")
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Ignore
        }
    }
}
