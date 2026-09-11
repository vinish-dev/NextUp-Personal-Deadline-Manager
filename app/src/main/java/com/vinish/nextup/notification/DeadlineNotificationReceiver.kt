package com.vinish.nextup.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.vinish.nextup.NextUpApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val deadlineId = intent.getLongExtra(NotificationHelper.EXTRA_DEADLINE_ID, -1L)
        if (deadlineId == -1L) return

        val app = context.applicationContext as? NextUpApplication
        val repository = app?.repository
        val scheduler = app?.notificationScheduler ?: DeadlineNotificationScheduler(context)

        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Deadline Reminder"
        val description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION)
        val dueText = intent.getStringExtra(NotificationHelper.EXTRA_DUE_TEXT)
        val priority = intent.getStringExtra(NotificationHelper.EXTRA_PRIORITY)

        when (action) {
            NotificationHelper.ACTION_DEADLINE_REMINDER -> {
                if (repository != null) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val deadline = repository.getDeadlineByIdOnce(deadlineId)
                            if (deadline == null || !deadline.isCompleted) {
                                val dynamicDueText = deadline?.let { scheduler.formatDueSubtitle(it) } ?: dueText
                                NotificationHelper.showDeadlineNotification(
                                    context = context,
                                    deadlineId = deadlineId,
                                    title = deadline?.title ?: title,
                                    description = deadline?.description ?: description,
                                    dueText = dynamicDueText,
                                    priority = deadline?.priority?.name ?: priority
                                )
                            }
                        } catch (_: Exception) {
                            NotificationHelper.showDeadlineNotification(
                                context = context,
                                deadlineId = deadlineId,
                                title = title,
                                description = description,
                                dueText = dueText,
                                priority = priority
                            )
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    NotificationHelper.showDeadlineNotification(
                        context = context,
                        deadlineId = deadlineId,
                        title = title,
                        description = description,
                        dueText = dueText,
                        priority = priority
                    )
                }
            }

            NotificationHelper.ACTION_SHOW_SNOOZE_OPTIONS -> {
                NotificationHelper.showSnoozeOptionsNotification(
                    context = context,
                    deadlineId = deadlineId,
                    title = title,
                    description = description,
                    dueText = dueText,
                    priority = priority
                )
            }

            NotificationHelper.ACTION_APPLY_SNOOZE -> {
                NotificationHelper.cancelNotification(context, deadlineId)

                val snoozeOption = intent.getStringExtra(NotificationHelper.EXTRA_SNOOZE_OPTION)
                    ?: NotificationHelper.SNOOZE_10_MINUTES

                val (snoozeDelayMillis, toastMessage) = when (snoozeOption) {
                    NotificationHelper.SNOOZE_1_HOUR -> {
                        Pair(60 * 60 * 1000L, "Reminder snoozed for 1 hour")
                    }
                    NotificationHelper.SNOOZE_TOMORROW -> {
                        Pair(24 * 60 * 60 * 1000L, "Reminder snoozed for tomorrow")
                    }
                    else -> {
                        Pair(10 * 60 * 1000L, "Reminder snoozed for 10 minutes")
                    }
                }

                val triggerTime = System.currentTimeMillis() + snoozeDelayMillis
                scheduler.scheduleSnooze(
                    deadlineId = deadlineId,
                    triggerEpochMillis = triggerTime,
                    title = title,
                    description = description,
                    dueText = dueText,
                    priority = priority
                )

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                }
            }

            NotificationHelper.ACTION_MARK_COMPLETED -> {
                NotificationHelper.cancelNotification(context, deadlineId)
                scheduler.cancel(deadlineId)

                if (repository != null) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.updateCompletionStatus(deadlineId, true)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Deadline marked as completed", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Deadline marked as completed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
