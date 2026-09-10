package com.vinish.nextup.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Reminder
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DeadlineNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun schedule(deadline: Deadline) {
        if (deadline.reminder == null || deadline.reminder == Reminder.NONE || deadline.isCompleted) {
            cancel(deadline.id)
            return
        }

        val triggerEpochMillis = calculateTriggerTimeMillis(deadline) ?: run {
            cancel(deadline.id)
            return
        }

        val now = System.currentTimeMillis()
        if (triggerEpochMillis <= now) {
            // Reminder time has already passed
            cancel(deadline.id)
            return
        }

        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = NotificationHelper.ACTION_DEADLINE_REMINDER
            putExtra(NotificationHelper.EXTRA_DEADLINE_ID, deadline.id)
            putExtra(NotificationHelper.EXTRA_TITLE, deadline.title)
            putExtra(NotificationHelper.EXTRA_DESCRIPTION, deadline.description)
            putExtra(NotificationHelper.EXTRA_DUE_TEXT, formatDueSubtitle(deadline))
            putExtra(NotificationHelper.EXTRA_PRIORITY, deadline.priority.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadline.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { manager ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (manager.canScheduleExactAlarms()) {
                            manager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerEpochMillis,
                                pendingIntent
                            )
                        } else {
                            manager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerEpochMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        manager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerEpochMillis,
                            pendingIntent
                        )
                    }
                } else {
                    manager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                try {
                    manager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } catch (_: Exception) {
                    // Fail gracefully
                }
            }
        }
    }

    fun cancel(deadlineId: Long) {
        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = NotificationHelper.ACTION_DEADLINE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadlineId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        NotificationHelper.cancelNotification(context, deadlineId)
    }

    private fun calculateTriggerTimeMillis(deadline: Deadline): Long? {
        val targetTime = deadline.dueTime ?: LocalTime.of(9, 0)
        val dueDateTime = LocalDateTime.of(deadline.dueDate, targetTime)

        val reminder = deadline.reminder ?: return null
        val reminderDateTime = when (reminder) {
            Reminder.NONE -> return null
            Reminder.AT_TIME -> dueDateTime
            Reminder.TEN_MINUTES_BEFORE -> dueDateTime.minusMinutes(10)
            Reminder.ONE_HOUR_BEFORE -> dueDateTime.minusHours(1)
            Reminder.ONE_DAY_BEFORE -> dueDateTime.minusDays(1)
            Reminder.THREE_DAYS_BEFORE -> dueDateTime.minusDays(3)
            Reminder.SEVEN_DAYS_BEFORE -> dueDateTime.minusDays(7)
        }

        return reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun formatDueSubtitle(deadline: Deadline): String {
        return if (deadline.dueTime != null) {
            "Due: ${deadline.dueDate} at ${deadline.dueTime}"
        } else {
            "Due: ${deadline.dueDate}"
        }
    }
}
