package com.vinish.nextup.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Recurrence
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.model.Subtask
import java.time.LocalDate
import java.time.LocalTime

class DeadlineConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.fromName(value)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (e: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromReminder(reminder: Reminder?): String? = reminder?.name

    @TypeConverter
    fun toReminder(value: String?): Reminder? = value?.let {
        try {
            Reminder.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromRecurrence(recurrence: Recurrence?): String? = recurrence?.name

    @TypeConverter
    fun toRecurrence(value: String?): Recurrence? = value?.let {
        try {
            Recurrence.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromSubtasksList(subtasks: List<Subtask>?): String =
        gson.toJson(subtasks ?: emptyList<Subtask>())

    @TypeConverter
    fun toSubtasksList(value: String?): List<Subtask> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<Subtask>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
