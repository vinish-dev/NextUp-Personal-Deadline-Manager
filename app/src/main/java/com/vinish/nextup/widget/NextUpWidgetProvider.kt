package com.vinish.nextup.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.vinish.nextup.MainActivity
import com.vinish.nextup.R
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.ui.quickadd.QuickAddActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class NextUpWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        const val SECTION_TODAY = "Today"
        const val SECTION_TOMORROW = "Tomorrow"
        const val SECTION_THIS_WEEK = "This Week"
        const val SECTION_SOMEDAY = "Someday"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, NextUpWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                updateWidgets(context, appWidgetManager, ids)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val activeDeadlines = try {
                    db.deadlineDao().getActiveDeadlines()
                } catch (e: Exception) {
                    emptyList()
                }

                val today = LocalDate.now()
                val tomorrow = today.plusDays(1)
                val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                val todayCount = activeDeadlines.count { it.dueDate.isEqual(today) }
                val tomorrowCount = activeDeadlines.count { it.dueDate.isEqual(tomorrow) }
                val thisWeekCount = activeDeadlines.count {
                    it.dueDate.isAfter(tomorrow) && !it.dueDate.isAfter(endOfWeek)
                }
                val somedayCount = activeDeadlines.count { it.dueDate.isAfter(endOfWeek) }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_minimal).apply {
                        // Counts
                        setTextViewText(R.id.tv_today_count, todayCount.toString())
                        setTextViewText(R.id.tv_tomorrow_count, tomorrowCount.toString())
                        setTextViewText(R.id.tv_this_week_count, thisWeekCount.toString())
                        setTextViewText(R.id.tv_someday_count, somedayCount.toString())

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

                        // Section taps -> QuickAddActivity with section
                        setOnClickPendingIntent(
                            R.id.btn_section_today,
                            createQuickAddPendingIntent(context, SECTION_TODAY, 101)
                        )
                        setOnClickPendingIntent(
                            R.id.btn_section_tomorrow,
                            createQuickAddPendingIntent(context, SECTION_TOMORROW, 102)
                        )
                        setOnClickPendingIntent(
                            R.id.btn_section_this_week,
                            createQuickAddPendingIntent(context, SECTION_THIS_WEEK, 103)
                        )
                        setOnClickPendingIntent(
                            R.id.btn_section_someday,
                            createQuickAddPendingIntent(context, SECTION_SOMEDAY, 104)
                        )
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun createQuickAddPendingIntent(
            context: Context,
            section: String,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, QuickAddActivity::class.java).apply {
                putExtra(QuickAddActivity.EXTRA_SECTION, section)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
