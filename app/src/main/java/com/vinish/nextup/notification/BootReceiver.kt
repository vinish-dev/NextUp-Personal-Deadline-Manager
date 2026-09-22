package com.vinish.nextup.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vinish.nextup.NextUpApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? NextUpApplication ?: return
            val repository = app.repository
            val scheduler = DeadlineNotificationScheduler(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allDeadlines = repository.allDeadlines.first()
                    allDeadlines.forEach { deadline ->
                        if (!deadline.isCompleted) {
                            scheduler.schedule(deadline)
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
