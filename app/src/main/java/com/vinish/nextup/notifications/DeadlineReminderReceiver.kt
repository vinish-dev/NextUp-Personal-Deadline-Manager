package com.vinish.nextup.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vinish.nextup.MainActivity

class DeadlineReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val deadlineId = intent.getLongExtra(EXTRA_DEADLINE_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Upcoming Deadline"
        val dueInfo = intent.getStringExtra(EXTRA_DUE_INFO) ?: "Due soon"
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Deadline"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            deadlineId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DeadlineNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("$category • $dueInfo")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$category • $dueInfo\nTap to view details in NextUp."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(deadlineId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission might have been revoked
        }
    }

    companion object {
        const val EXTRA_DEADLINE_ID = "extra_deadline_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DUE_INFO = "extra_due_info"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
