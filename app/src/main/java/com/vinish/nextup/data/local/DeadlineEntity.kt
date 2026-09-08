package com.vinish.nextup.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Recurrence
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.model.Subtask
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "deadlines")
data class DeadlineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate,
    val dueTime: LocalTime? = null,
    val category: Category,
    val priority: Priority,
    val reminder: Reminder? = null,
    val recurrence: Recurrence? = null,
    val subtasks: List<Subtask> = emptyList(),
    val isCompleted: Boolean = false
) {
    fun toDomain(): Deadline = Deadline(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime,
        category = category,
        priority = priority,
        reminder = reminder,
        recurrence = recurrence,
        subtasks = subtasks,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(deadline: Deadline): DeadlineEntity = DeadlineEntity(
            id = deadline.id,
            title = deadline.title,
            description = deadline.description,
            dueDate = deadline.dueDate,
            dueTime = deadline.dueTime,
            category = deadline.category,
            priority = deadline.priority,
            reminder = deadline.reminder,
            recurrence = deadline.recurrence,
            subtasks = deadline.subtasks,
            isCompleted = deadline.isCompleted
        )
    }
}
