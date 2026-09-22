package com.vinish.nextup.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.vinish.nextup.R
import com.vinish.nextup.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import androidx.core.graphics.toColorInt

class NextUpWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NextUpWidgetFactory(applicationContext)
    }
}

sealed class WidgetItem {
    data class SectionHeader(val section: String, val count: Int, val isOverdue: Boolean = false) : WidgetItem()
    data class TaskItem(val id: Long, val title: String, val isCompleted: Boolean, val section: String, val isOverdue: Boolean = false) : WidgetItem()
}

class NextUpWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val items = mutableListOf<WidgetItem>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        items.clear()
        val allDeadlines = try {
            runBlocking {
                AppDatabase.getDatabase(context).deadlineDao().getAllDeadlinesSync()
            }
        } catch (e: Exception) {
            emptyList()
        }

        val today = LocalDate.now()
        val nowTime = LocalTime.now()
        val tomorrow = today.plusDays(1)
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        fun isOverdue(dueDate: LocalDate, dueTime: LocalTime?): Boolean {
            return dueDate.isBefore(today) || (dueDate.isEqual(today) && dueTime != null && dueTime.isBefore(nowTime))
        }

        val overdueTasks = allDeadlines.filter { isOverdue(it.dueDate, it.dueTime) }
        val todayTasks = allDeadlines.filter { it.dueDate.isEqual(today) && !isOverdue(it.dueDate, it.dueTime) }
        val tomorrowTasks = allDeadlines.filter { it.dueDate.isEqual(tomorrow) }
        val thisWeekTasks = allDeadlines.filter {
            it.dueDate.isAfter(tomorrow) && !it.dueDate.isAfter(endOfWeek)
        }
        val somedayTasks = allDeadlines.filter { it.dueDate.isAfter(endOfWeek) }

        // Section 0: Overdue (shown when there are overdue tasks)
        if (overdueTasks.isNotEmpty()) {
            items.add(WidgetItem.SectionHeader(NextUpWidgetProvider.SECTION_OVERDUE, overdueTasks.count { !it.isCompleted }, isOverdue = true))
            overdueTasks.forEach {
                items.add(WidgetItem.TaskItem(it.id, it.title, it.isCompleted, NextUpWidgetProvider.SECTION_OVERDUE, isOverdue = true))
            }
        }

        // Section 1: Today
        items.add(WidgetItem.SectionHeader(NextUpWidgetProvider.SECTION_TODAY, todayTasks.count { !it.isCompleted }))
        todayTasks.forEach {
            items.add(WidgetItem.TaskItem(it.id, it.title, it.isCompleted, NextUpWidgetProvider.SECTION_TODAY))
        }

        // Section 2: Tomorrow
        items.add(WidgetItem.SectionHeader(NextUpWidgetProvider.SECTION_TOMORROW, tomorrowTasks.count { !it.isCompleted }))
        tomorrowTasks.forEach {
            items.add(WidgetItem.TaskItem(it.id, it.title, it.isCompleted, NextUpWidgetProvider.SECTION_TOMORROW))
        }

        // Section 3: This Week
        items.add(WidgetItem.SectionHeader(NextUpWidgetProvider.SECTION_THIS_WEEK, thisWeekTasks.count { !it.isCompleted }))
        thisWeekTasks.forEach {
            items.add(WidgetItem.TaskItem(it.id, it.title, it.isCompleted, NextUpWidgetProvider.SECTION_THIS_WEEK))
        }

        // Section 4: Someday
        items.add(WidgetItem.SectionHeader(NextUpWidgetProvider.SECTION_SOMEDAY, somedayTasks.count { !it.isCompleted }))
        somedayTasks.forEach {
            items.add(WidgetItem.TaskItem(it.id, it.title, it.isCompleted, NextUpWidgetProvider.SECTION_SOMEDAY))
        }
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= items.size) return null

        return when (val item = items[position]) {
            is WidgetItem.SectionHeader -> {
                RemoteViews(context.packageName, R.layout.widget_item_section_header).apply {
                    setTextViewText(R.id.tv_section_title, item.section)
                    setTextViewText(R.id.tv_section_count, item.count.toString())

                    if (item.isOverdue) {
                        setTextColor(R.id.tv_section_title, "#E53935".toColorInt())
                        setInt(R.id.tv_section_count, "setBackgroundResource", R.drawable.widget_badge_overdue_bg)
                        setTextColor(R.id.tv_section_count, "#E53935".toColorInt())
                        setTextColor(R.id.tv_section_add, "#E53935".toColorInt())
                    } else {
                        setTextColor(R.id.tv_section_title, "#1E2430".toColorInt())
                        setInt(R.id.tv_section_count, "setBackgroundResource", R.drawable.widget_badge_bg)
                        setTextColor(R.id.tv_section_count, "#166534".toColorInt())
                        setTextColor(R.id.tv_section_add, "#56AB7E".toColorInt())
                    }

                    // Tapping section header opens Quick Add with that section
                    val fillInIntent = Intent().apply {
                        action = NextUpWidgetProvider.ACTION_OPEN_QUICK_ADD
                        putExtra(NextUpWidgetProvider.EXTRA_SECTION, item.section)
                    }
                    setOnClickFillInIntent(R.id.widget_section_header_root, fillInIntent)
                }
            }
            is WidgetItem.TaskItem -> {
                RemoteViews(context.packageName, R.layout.widget_item_task).apply {
                    // Dot icon & toggle action
                    if (item.isCompleted) {
                        setImageViewResource(R.id.btn_task_dot, R.drawable.widget_dot_completed)
                    } else {
                        setImageViewResource(R.id.btn_task_dot, R.drawable.widget_dot_uncompleted)
                    }

                    val dotIntent = Intent().apply {
                        action = NextUpWidgetProvider.ACTION_TOGGLE_TASK
                        putExtra(NextUpWidgetProvider.EXTRA_TASK_ID, item.id)
                    }
                    setOnClickFillInIntent(R.id.btn_task_dot, dotIntent)

                    // Title with strikethrough if completed
                    if (item.isCompleted) {
                        val spannable = SpannableString(item.title).apply {
                            setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        setTextViewText(R.id.tv_task_title, spannable)
                        setTextColor(R.id.tv_task_title, Color.parseColor("#94A3B8"))
                    } else {
                        setTextViewText(R.id.tv_task_title, item.title)
                        setTextColor(R.id.tv_task_title, Color.parseColor("#1E2430"))
                    }

                    // Tapping title or row opens edit popup
                    val editIntent = Intent().apply {
                        action = NextUpWidgetProvider.ACTION_EDIT_TASK
                        putExtra(NextUpWidgetProvider.EXTRA_TASK_ID, item.id)
                        putExtra(NextUpWidgetProvider.EXTRA_SECTION, item.section)
                    }
                    setOnClickFillInIntent(R.id.tv_task_title, editIntent)
                    setOnClickFillInIntent(R.id.widget_task_item_root, editIntent)
                }
            }
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) {
            when (val item = items[position]) {
                is WidgetItem.TaskItem -> item.id
                is WidgetItem.SectionHeader -> -position.toLong() - 1L
            }
        } else {
            position.toLong()
        }
    }

    override fun hasStableIds(): Boolean = true
}
