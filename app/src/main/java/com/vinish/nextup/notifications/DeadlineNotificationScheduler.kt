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

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
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

        val dueInfo = if (deadline.dueTime != null) {
            val timeStr = deadline.dueTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
            "Due on ${deadline.dueDate} at $timeStr"
        } else {
            "Due on ${deadline.dueDate}"
        }

        val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            putExtra(DeadlineReminderReceiver.EXTRA_DEADLINE_ID, deadline.id)
            putExtra(DeadlineReminderReceiver.EXTRA_TITLE, deadline.title)
            putExtra(DeadlineReminderReceiver.EXTRA_DUE_INFO, dueInfo)
            putExtra(DeadlineReminderReceiver.EXTRA_CATEGORY, deadline.category.displayName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            deadline.id.toInt(),
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
