package com.vinish.nextup.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase
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

        var triggerEpochMillis = calculateTriggerTimeMillis(deadline) ?: run {
            cancel(deadline.id)
            return
        }

        val now = System.currentTimeMillis()
        if (triggerEpochMillis <= now) {
            val targetTime = deadline.dueTime ?: LocalTime.of(23, 59)
            val dueDateTime = LocalDateTime.of(deadline.dueDate, targetTime)
            val dueEpochMillis = dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (dueEpochMillis > now) {
                // Deadline is still upcoming, but the reminder threshold (e.g., 10m/1h before) has passed.
                // Trigger in 2 seconds so the user is immediately reminded.
                triggerEpochMillis = now + 2000L
            } else {
                // The deadline itself has already passed
                cancel(deadline.id)
                return
            }
        }

        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = NotificationHelper.ACTION_DEADLINE_REMINDER
            putExtra(NotificationHelper.EXTRA_DEADLINE_ID, deadline.id)
            putExtra(NotificationHelper.EXTRA_TITLE, deadline.title.toTitleCase())
            putExtra(NotificationHelper.EXTRA_DESCRIPTION, deadline.description?.toSentenceCase())
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
        val targetTime = deadline.dueTime ?: if (deadline.dueDate == java.time.LocalDate.now()) {
            LocalTime.now().plusHours(1)
        } else {
            LocalTime.of(9, 0)
        }
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

    fun scheduleSnooze(
        deadlineId: Long,
        triggerEpochMillis: Long,
        title: String,
        description: String?,
        dueText: String?,
        priority: String?
    ) {
        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            action = NotificationHelper.ACTION_DEADLINE_REMINDER
            putExtra(NotificationHelper.EXTRA_DEADLINE_ID, deadlineId)
            putExtra(NotificationHelper.EXTRA_TITLE, title.toTitleCase())
            putExtra(NotificationHelper.EXTRA_DESCRIPTION, description?.toSentenceCase())
            putExtra(NotificationHelper.EXTRA_DUE_TEXT, dueText)
            putExtra(NotificationHelper.EXTRA_PRIORITY, priority)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadlineId.toInt(),
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
            } catch (e: Exception) {
                android.util.Log.e("DeadlineNotification", "Failed to schedule snooze alarm", e)
            }
        }
    }

    fun formatDueSubtitle(deadline: Deadline): String {
        val targetTime = deadline.dueTime ?: LocalTime.of(23, 59)
        val dueDateTime = LocalDateTime.of(deadline.dueDate, targetTime)
        val now = LocalDateTime.now()

        val timeDiffMinutes = java.time.Duration.between(now, dueDateTime).toMinutes()

        val timingText = when {
            // Overdue
            timeDiffMinutes < 0 -> {
                val overdueMinutes = kotlin.math.abs(timeDiffMinutes)
                when {
                    overdueMinutes < 60 -> "Overdue by $overdueMinutes minutes"
                    overdueMinutes < 1440 -> {
                        val hours = overdueMinutes / 60
                        val mins = overdueMinutes % 60
                        if (mins == 0L) "Overdue by $hours hour${if (hours > 1) "s" else ""}"
                        else "Overdue by ${hours}h ${mins}m"
                    }
                    else -> {
                        val days = overdueMinutes / 1440
                        "Overdue by $days day${if (days > 1) "s" else ""}"
                    }
                }
            }
            // Due right now (within 1 minute)
            timeDiffMinutes in 0..1 -> "Due now"
            // Due in minutes
            timeDiffMinutes < 60 -> "Due in $timeDiffMinutes minutes"
            // Due in hours today
            timeDiffMinutes < 720 && deadline.dueDate.isEqual(java.time.LocalDate.now()) -> {
                val hours = timeDiffMinutes / 60
                "Due in $hours hour${if (hours > 1) "s" else ""}"
            }
            // Due tomorrow
            deadline.dueDate.isEqual(java.time.LocalDate.now().plusDays(1)) -> {
                if (deadline.dueTime != null) {
                    val timeFormatted = deadline.dueTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH))
                    "Due tomorrow at $timeFormatted"
                } else {
                    "Due tomorrow"
                }
            }
            // Due in days
            else -> {
                val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), deadline.dueDate)
                if (daysDiff > 1) "Due in $daysDiff days"
                else if (deadline.dueTime != null) {
                    val timeFormatted = deadline.dueTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH))
                    "Due today at $timeFormatted"
                } else {
                    "Due today"
                }
            }
        }

        return timingText
    }
}
