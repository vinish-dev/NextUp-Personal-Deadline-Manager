package com.vinish.nextup.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vinish.nextup.MainActivity
import com.vinish.nextup.R
import com.vinish.nextup.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeadlineReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_SHOW_REMINDER

        if (action == ACTION_DISMISS_SUGGESTION) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (notificationId != -1) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            return
        }

        if (action == ACTION_AUTOFILL_ADD) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (notificationId != -1) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }

            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Captured Deadline"
            val description = intent.getStringExtra(EXTRA_DESCRIPTION)
            val dateStr = intent.getStringExtra(EXTRA_DUE_DATE)
            val timeStr = intent.getStringExtra(EXTRA_DUE_TIME)
            val categoryStr = intent.getStringExtra(EXTRA_CATEGORY) ?: "PERSONAL"
            val priorityStr = intent.getStringExtra(EXTRA_PRIORITY) ?: "MEDIUM"

            val dueDate = dateStr?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() } ?: java.time.LocalDate.now()
            val dueTime = timeStr?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() }
            val category = runCatching { com.vinish.nextup.model.Category.valueOf(categoryStr) }.getOrDefault(com.vinish.nextup.model.Category.PERSONAL)
            val priority = runCatching { com.vinish.nextup.model.Priority.valueOf(priorityStr) }.getOrDefault(com.vinish.nextup.model.Priority.MEDIUM)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val newDeadline = com.vinish.nextup.model.Deadline(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        category = category,
                        priority = priority,
                        reminder = if (dueTime != null) com.vinish.nextup.model.Reminder.AT_TIME else com.vinish.nextup.model.Reminder.ONE_DAY_BEFORE,
                        isCompleted = false
                    )
                    val id = db.deadlineDao().insertDeadline(
                        com.vinish.nextup.data.local.DeadlineEntity.fromDomain(newDeadline)
                    )
                    DeadlineNotificationScheduler.scheduleReminder(context, newDeadline.copy(id = id))
                } catch (_: Exception) {
                    // Ignore
                } finally {
                    pendingResult.finish()
                }
            }
            Toast.makeText(context, "Added to NextUp: $title", Toast.LENGTH_SHORT).show()
            return
        }

        val deadlineId = intent.getLongExtra(EXTRA_DEADLINE_ID, -1L)
        if (deadlineId == -1L) return

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Upcoming Deadline"
        val dueInfo = intent.getStringExtra(EXTRA_DUE_INFO) ?: "Due soon"
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "General"
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val priority = intent.getStringExtra(EXTRA_PRIORITY) ?: "MEDIUM"
        val subtasks = intent.getStringArrayListExtra(EXTRA_SUBTASKS)

        when (action) {
            ACTION_MARK_DONE -> {
                NotificationManagerCompat.from(context).cancel(deadlineId.toInt())
                DeadlineNotificationScheduler.cancelReminder(context, deadlineId)

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        database.deadlineDao().updateCompletionStatus(deadlineId, true)
                    } catch (_: Exception) {
                        // Ignore failure
                    } finally {
                        pendingResult.finish()
                    }
                }
                Toast.makeText(context, "Completed: $title", Toast.LENGTH_SHORT).show()
            }

            ACTION_SNOOZE -> {
                NotificationManagerCompat.from(context).cancel(deadlineId.toInt())
                DeadlineNotificationScheduler.scheduleSnooze(
                    context = context,
                    deadlineId = deadlineId,
                    title = title,
                    dueInfo = dueInfo,
                    category = category,
                    description = description,
                    priority = priority,
                    pendingSubtasks = subtasks,
                    minutes = 60
                )
                Toast.makeText(context, "Snoozed for 1 hour", Toast.LENGTH_SHORT).show()
            }

            else -> {
                showNotification(
                    context = context,
                    deadlineId = deadlineId,
                    title = title,
                    dueInfo = dueInfo,
                    category = category,
                    description = description,
                    priority = priority,
                    subtasks = subtasks
                )
            }
        }
    }

    private fun showNotification(
        context: Context,
        deadlineId: Long,
        title: String,
        dueInfo: String,
        category: String,
        description: String?,
        priority: String,
        subtasks: ArrayList<String>?
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            deadlineId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markDoneIntent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
            putExtra(EXTRA_TITLE, title)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (deadlineId * 10 + 1).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_DEADLINE_ID, deadlineId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DUE_INFO, dueInfo)
            putExtra(EXTRA_CATEGORY, category)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_PRIORITY, priority)
            putStringArrayListExtra(EXTRA_SUBTASKS, subtasks)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (deadlineId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = buildString {
            append(dueInfo)
            if (!description.isNullOrBlank()) {
                append("\n\n")
                append(description)
            }
            if (!subtasks.isNullOrEmpty()) {
                append("\n\nPending Subtasks:")
                subtasks.take(3).forEach { task ->
                    append("\n• ").append(task)
                }
                if (subtasks.size > 3) {
                    append("\n• +").append(subtasks.size - 3).append(" more")
                }
            }
        }

        val accentColor = if (priority == "HIGH") 0xFFE53935.toInt() else 0xFF1E6BFF.toInt()

        val notification = NotificationCompat.Builder(context, DeadlineNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setContentTitle(title)
            .setContentText(dueInfo)
            .setSubText(category)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_check_action, "Mark Done", markDonePendingIntent)
            .addAction(R.drawable.ic_snooze_action, "Snooze 1h", snoozePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(deadlineId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission might have been revoked
        }
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.vinish.nextup.ACTION_SHOW_REMINDER"
        const val ACTION_MARK_DONE = "com.vinish.nextup.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.vinish.nextup.ACTION_SNOOZE"
        const val ACTION_AUTOFILL_ADD = "com.vinish.nextup.ACTION_AUTOFILL_ADD"
        const val ACTION_DISMISS_SUGGESTION = "com.vinish.nextup.ACTION_DISMISS_SUGGESTION"

        const val EXTRA_DEADLINE_ID = "extra_deadline_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DUE_INFO = "extra_due_info"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_PRIORITY = "extra_priority"
        const val EXTRA_SUBTASKS = "extra_subtasks"
        const val EXTRA_DUE_DATE = "extra_due_date"
        const val EXTRA_DUE_TIME = "extra_due_time"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
