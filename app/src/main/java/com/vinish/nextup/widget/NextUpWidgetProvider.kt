package com.vinish.nextup.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.vinish.nextup.MainActivity
import com.vinish.nextup.R
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.ui.quickadd.QuickAddActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NextUpWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val task = db.deadlineDao().getDeadlineByIdOnce(taskId)
                            if (task != null) {
                                db.deadlineDao().updateCompletionStatus(taskId, !task.isCompleted)
                                updateAllWidgets(context)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_OPEN_QUICK_ADD -> {
                val section = intent.getStringExtra(EXTRA_SECTION) ?: SECTION_TODAY
                val quickAddIntent = Intent(context, QuickAddActivity::class.java).apply {
                    putExtra(QuickAddActivity.EXTRA_SECTION, section)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(quickAddIntent)
            }
            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val SECTION_TODAY = "Today"
        const val SECTION_TOMORROW = "Tomorrow"
        const val SECTION_THIS_WEEK = "This Week"
        const val SECTION_SOMEDAY = "Someday"

        const val ACTION_TOGGLE_TASK = "com.vinish.nextup.ACTION_TOGGLE_TASK"
        const val ACTION_OPEN_QUICK_ADD = "com.vinish.nextup.ACTION_OPEN_QUICK_ADD"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_SECTION = "extra_section"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, NextUpWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                // Refresh data in ListView
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
                for (id in ids) {
                    updateSingleWidget(context, appWidgetManager, id)
                }
            }
        }

        fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_minimal).apply {
                // Header tap -> Open Main App
                val mainAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val mainAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_header, mainAppPendingIntent)

                // Quick Add shortcut on "+ Add" header button
                val addIntent = Intent(context, QuickAddActivity::class.java).apply {
                    putExtra(QuickAddActivity.EXTRA_SECTION, SECTION_TODAY)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val addPendingIntent = PendingIntent.getActivity(
                    context,
                    1,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.btn_widget_quick_add, addPendingIntent)

                // Set up RemoteViewsService adapter for the ListView
                val serviceIntent = Intent(context, NextUpWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                setRemoteAdapter(appWidgetId, R.id.widget_list_view, serviceIntent)
                setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

                // PendingIntent template for ListView items
                val templateIntent = Intent(context, NextUpWidgetProvider::class.java)
                val pendingIntentTemplate = PendingIntent.getBroadcast(
                    context,
                    200,
                    templateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                setPendingIntentTemplate(R.id.widget_list_view, pendingIntentTemplate)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
