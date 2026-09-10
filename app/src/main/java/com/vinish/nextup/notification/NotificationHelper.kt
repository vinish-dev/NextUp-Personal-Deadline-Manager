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

object NotificationHelper {

    const val CHANNEL_ID = "deadline_reminders_channel"
    const val CHANNEL_NAME = "Deadline Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for upcoming and due deadlines"

    const val ACTION_DEADLINE_REMINDER = "com.vinish.nextup.ACTION_DEADLINE_REMINDER"
    const val ACTION_MARK_COMPLETED = "com.vinish.nextup.ACTION_MARK_COMPLETED"

    const val EXTRA_DEADLINE_ID = "extra_deadline_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESCRIPTION = "extra_description"
    const val EXTRA_DUE_TEXT = "extra_due_text"
    const val EXTRA_PRIORITY = "extra_priority"

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

        val contentMessage = buildString {
            if (!dueText.isNullOrBlank()) {
                append(dueText)
            }
            if (!priority.isNullOrBlank()) {
                if (isNotEmpty()) append(" • ")
                append("Priority: $priority")
            }
            if (!description.isNullOrBlank()) {
                if (isNotEmpty()) append("\n")
                append(description)
            }
        }.ifEmpty { "Upcoming deadline" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark as Completed",
                markCompletePendingIntent
            )
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(deadlineId.toInt(), notification)
        } catch (_: SecurityException) {
            // In case POST_NOTIFICATIONS permission was revoked by user
        }
    }

    fun cancelNotification(context: Context, deadlineId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(deadlineId.toInt())
    }
}
