package com.vinish.nextup.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        when (action) {
            NotificationHelper.ACTION_DEADLINE_REMINDER -> {
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Deadline Reminder"
                val description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION)
                val dueText = intent.getStringExtra(NotificationHelper.EXTRA_DUE_TEXT)
                val priority = intent.getStringExtra(NotificationHelper.EXTRA_PRIORITY)

                if (repository != null) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val deadline = repository.getDeadlineByIdOnce(deadlineId)
                            if (deadline == null || !deadline.isCompleted) {
                                NotificationHelper.showDeadlineNotification(
                                    context = context,
                                    deadlineId = deadlineId,
                                    title = deadline?.title ?: title,
                                    description = deadline?.description ?: description,
                                    dueText = dueText,
                                    priority = deadline?.priority?.name ?: priority
                                )
                            }
                        } catch (_: Exception) {
                            // In case of error, show from intent extras
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

            NotificationHelper.ACTION_MARK_COMPLETED -> {
                NotificationHelper.cancelNotification(context, deadlineId)
                if (repository != null) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.updateCompletionStatus(deadlineId, true)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
