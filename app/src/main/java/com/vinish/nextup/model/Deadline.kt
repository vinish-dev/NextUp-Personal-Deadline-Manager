package com.vinish.nextup.model

import java.time.LocalDate
import java.time.LocalTime

data class Deadline(
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate,
    val dueTime: LocalTime? = null,
    val category: Category,
    val priority: Priority,
    val reminder: Reminder? = null,
    val recurrence: Recurrence? = null,
    val isCompleted: Boolean = false
)
