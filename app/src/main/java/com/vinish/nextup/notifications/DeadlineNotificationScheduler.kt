package com.vinish.nextup.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Reminder
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DeadlineNotificationScheduler {

    const val CHANNEL_ID = "deadline_reminders_channel"
    private const val CHANNEL_NAME = "Deadline Reminders"
    private const val CHANNEL_DESC = "Notifications and timely alerts for your personal deadlines"

    const val SMART_CAPTURE_CHANNEL_ID = "smart_capture_channel"
    private const val SMART_CAPTURE_CHANNEL_NAME = "Smart Deadline Suggestions"
    private const val SMART_CAPTURE_CHANNEL_DESC = "Instant prompts to add deadlines detected from other apps"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val captureChannel = NotificationChannel(
                SMART_CAPTURE_CHANNEL_ID,
                SMART_CAPTURE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = SMART_CAPTURE_CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager?.createNotificationChannel(reminderChannel)
            notificationManager?.createNotificationChannel(captureChannel)
        }
    }

    fun formatDueInfo(dueDate: java.time.LocalDate, dueTime: LocalTime?): String {
        val today = java.time.LocalDate.now()
        val isToday = dueDate.isEqual(today)
        val isTomorrow = dueDate.isEqual(today.plusDays(1))
        val isYesterday = dueDate.isEqual(today.minusDays(1))
        val isOverdue = dueDate.isBefore(today)

        val datePart = when {
            isToday -> "Due today"
            isTomorrow -> "Due tomorrow"
            isYesterday -> "Overdue (yesterday)"
            isOverdue -> "Overdue"
            dueDate.year == today.year -> "Due " + dueDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH))
            else -> "Due " + dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
        }

        return if (dueTime != null) {
            val timePart = dueTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
            "$datePart at $timePart"
        } else {
            datePart
        }
    }

    fun scheduleReminder(context: Context, deadline: Deadline) {
        if (deadline.reminder == Reminder.NONE || deadline.isCompleted) {
            cancelReminder(context, deadline.id)
            return
        }

        val dueTime = deadline.dueTime ?: LocalTime.of(9, 0)
        val dueDateTime = LocalDateTime.of(deadline.dueDate, dueTime)

        val triggerDateTime = when (deadline.reminder) {
            null, Reminder.NONE -> return
            Reminder.AT_TIME -> dueDateTime
            Reminder.TEN_MINUTES_BEFORE -> dueDateTime.minusMinutes(10)
            Reminder.ONE_HOUR_BEFORE -> dueDateTime.minusHours(1)
            Reminder.ONE_DAY_BEFORE -> dueDateTime.minusDays(1)
            Reminder.THREE_DAYS_BEFORE -> dueDateTime.minusDays(3)
            Reminder.SEVEN_DAYS_BEFORE -> dueDateTime.minusDays(7)
        }

        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerMillis <= System.currentTimeMillis()) {
            return
        }

        val dueInfo = formatDueInfo(deadline.dueDate, deadline.dueTime)
        val pendingSubtasks = ArrayList(deadline.subtasks.filter { !it.isCompleted }.map { it.title })

        val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            action = DeadlineReminderReceiver.ACTION_SHOW_REMINDER
            putExtra(DeadlineReminderReceiver.EXTRA_DEADLINE_ID, deadline.id)
            putExtra(DeadlineReminderReceiver.EXTRA_TITLE, deadline.title)
            putExtra(DeadlineReminderReceiver.EXTRA_DUE_INFO, dueInfo)
            putExtra(DeadlineReminderReceiver.EXTRA_CATEGORY, deadline.category.displayName)
            putExtra(DeadlineReminderReceiver.EXTRA_DESCRIPTION, deadline.description)
            putExtra(DeadlineReminderReceiver.EXTRA_PRIORITY, deadline.priority.name)
            putStringArrayListExtra(DeadlineReminderReceiver.EXTRA_SUBTASKS, pendingSubtasks)
        }

        scheduleAlarm(context, deadline.id, triggerMillis, intent)
    }

    fun scheduleSnooze(
        context: Context,
        deadlineId: Long,
        title: String,
        dueInfo: String,
        category: String,
        description: String? = null,
        priority: String? = null,
        pendingSubtasks: ArrayList<String>? = null,
        minutes: Long = 60
    ) {
        val triggerMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            action = DeadlineReminderReceiver.ACTION_SHOW_REMINDER
            putExtra(DeadlineReminderReceiver.EXTRA_DEADLINE_ID, deadlineId)
            putExtra(DeadlineReminderReceiver.EXTRA_TITLE, title)
            putExtra(DeadlineReminderReceiver.EXTRA_DUE_INFO, dueInfo)
            putExtra(DeadlineReminderReceiver.EXTRA_CATEGORY, category)
            putExtra(DeadlineReminderReceiver.EXTRA_DESCRIPTION, description)
            putExtra(DeadlineReminderReceiver.EXTRA_PRIORITY, priority)
            putStringArrayListExtra(DeadlineReminderReceiver.EXTRA_SUBTASKS, pendingSubtasks)
        }

        scheduleAlarm(context, deadlineId, triggerMillis, intent)
    }

    private fun scheduleAlarm(context: Context, deadlineId: Long, triggerMillis: Long, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadlineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, deadlineId: Long) {
        val intent = Intent(context, DeadlineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadlineId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
