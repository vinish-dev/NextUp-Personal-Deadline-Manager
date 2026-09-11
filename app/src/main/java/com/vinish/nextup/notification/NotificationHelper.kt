package com.vinish.nextup.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vinish.nextup.MainActivity
import com.vinish.nextup.R
import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase

object NotificationHelper {

    const val CHANNEL_ID = "deadline_reminders_channel"
    const val CHANNEL_NAME = "Deadline Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for upcoming and due deadlines"

    const val ACTION_DEADLINE_REMINDER = "com.vinish.nextup.ACTION_DEADLINE_REMINDER"
    const val ACTION_MARK_COMPLETED = "com.vinish.nextup.ACTION_MARK_COMPLETED"
    const val ACTION_SHOW_SNOOZE_OPTIONS = "com.vinish.nextup.ACTION_SHOW_SNOOZE_OPTIONS"
    const val ACTION_APPLY_SNOOZE = "com.vinish.nextup.ACTION_APPLY_SNOOZE"

    const val EXTRA_DEADLINE_ID = "extra_deadline_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESCRIPTION = "extra_description"
    const val EXTRA_DUE_TEXT = "extra_due_text"
    const val EXTRA_PRIORITY = "extra_priority"
    const val EXTRA_SNOOZE_OPTION = "extra_snooze_option"

    const val SNOOZE_10_MINUTES = "snooze_10_minutes"
    const val SNOOZE_1_HOUR = "snooze_1_hour"
    const val SNOOZE_TOMORROW = "snooze_tomorrow"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showDeadlineNotification(
        context: Context,
        deadlineId: Long,
        title: String,
        description: String?,
        dueText: String?,
        priority: String?
    ) {
        createNotificationChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
        }

        val tapPendingIntent = PendingIntent.getActivity(
            context,
            deadlineId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Mark as Completed
        val markCompleteIntent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETED
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
        }

        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            (deadlineId + 100000).toInt(),
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Snooze
        val snoozeIntent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = ACTION_SHOW_SNOOZE_OPTIONS
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_DUE_TEXT, dueText)
            putExtra(EXTRA_PRIORITY, priority)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (deadlineId + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentMessage = buildString {
            if (!dueText.isNullOrBlank()) {
                append(dueText)
            }
            if (!description.isNullOrBlank()) {
                if (isNotEmpty()) append("\n")
                append(description.toSentenceCase())
            }
        }.ifEmpty { "Upcoming deadline" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF2563EB.toInt())
            .setContentTitle(title.toTitleCase())
            .setContentText(contentMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .addAction(
                0,
                "Mark as Completed",
                markCompletePendingIntent
            )
            .addAction(
                0,
                "Snooze",
                snoozePendingIntent
            )
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(deadlineId.toInt(), notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Failed to post notification", e)
        }
    }

    fun showSnoozeOptionsNotification(
        context: Context,
        deadlineId: Long,
        title: String,
        description: String?,
        dueText: String?,
        priority: String?
    ) {
        createNotificationChannel(context)

        fun createSnoozePendingIntent(option: String, requestCodeOffset: Int): PendingIntent {
            val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
                action = ACTION_APPLY_SNOOZE
                putExtra(EXTRA_DEADLINE_ID, deadlineId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_DUE_TEXT, dueText)
                putExtra(EXTRA_PRIORITY, priority)
                putExtra(EXTRA_SNOOZE_OPTION, option)
            }
            return PendingIntent.getBroadcast(
                context,
                (deadlineId + requestCodeOffset).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snooze10MinPendingIntent = createSnoozePendingIntent(SNOOZE_10_MINUTES, 300000)
        val snooze1HourPendingIntent = createSnoozePendingIntent(SNOOZE_1_HOUR, 400000)
        val snoozeTomorrowPendingIntent = createSnoozePendingIntent(SNOOZE_TOMORROW, 500000)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            deadlineId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF2563EB.toInt())
            .setContentTitle("Snooze: ${title.toTitleCase()}")
            .setContentText("Select snooze duration:")
            .setStyle(NotificationCompat.BigTextStyle().bigText("When would you like to be reminded again?"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .addAction(0, "10 minutes", snooze10MinPendingIntent)
            .addAction(0, "1 hour", snooze1HourPendingIntent)
            .addAction(0, "Tomorrow", snoozeTomorrowPendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(deadlineId.toInt(), notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Failed to update snooze notification", e)
        }
    }

    fun cancelNotification(context: Context, deadlineId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(deadlineId.toInt())
    }
}
