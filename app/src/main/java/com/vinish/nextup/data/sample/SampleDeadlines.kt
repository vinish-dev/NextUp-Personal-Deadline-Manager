package com.vinish.nextup.data.sample

import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Reminder
import java.time.LocalDate
import java.time.LocalTime

object SampleDeadlines {
    val sampleDeadlines = listOf(
        Deadline(
            id = 1L,
            title = "DBMS Assignment",
            description = "Complete chapters 4 & 5 normalization and SQL queries",
            dueDate = LocalDate.now(),
            dueTime = LocalTime.of(23, 59),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            reminder = Reminder.ONE_HOUR_BEFORE,
            isCompleted = false
        ),
        Deadline(
            id = 2L,
            title = "Sceptix Project Submission",
            description = "Final submission of full-stack project repository and documentation",
            dueDate = LocalDate.now(),
            dueTime = LocalTime.of(23, 59),
            category = Category.WORK,
            priority = Priority.HIGH,
            reminder = Reminder.AT_TIME,
            isCompleted = false
        ),
        Deadline(
            id = 3L,
            title = "College Application",
            description = "Review and submit application form and transcripts",
            dueDate = LocalDate.now().plusDays(1),
            dueTime = LocalTime.of(17, 0),
            category = Category.DOCUMENTS,
            priority = Priority.MEDIUM,
            reminder = Reminder.ONE_DAY_BEFORE,
            isCompleted = false
        ),
        Deadline(
            id = 4L,
            title = "Algorithm Problem Set",
            description = "Dynamic programming and graph problems",
            dueDate = LocalDate.now().plusDays(1),
            dueTime = LocalTime.of(20, 0),
            category = Category.EDUCATION,
            priority = Priority.MEDIUM,
            reminder = Reminder.ONE_HOUR_BEFORE,
            isCompleted = false
        ),
        Deadline(
            id = 5L,
            title = "Electricity Bill",
            description = "Pay monthly utility bill via electricity portal",
            dueDate = LocalDate.now().plusDays(3),
            dueTime = LocalTime.of(18, 0),
            category = Category.FINANCE,
            priority = Priority.MEDIUM,
            reminder = Reminder.ONE_DAY_BEFORE,
            isCompleted = false
        ),
        Deadline(
            id = 6L,
            title = "Gym Membership Renewal",
            description = "Annual membership renewal fee",
            dueDate = LocalDate.now().plusDays(5),
            dueTime = LocalTime.of(10, 0),
            category = Category.PERSONAL,
            priority = Priority.LOW,
            reminder = Reminder.NONE,
            isCompleted = false
        ),
        Deadline(
            id = 7L,
            title = "UI/UX Portfolio Review",
            description = "Present redesigned case study to mentor",
            dueDate = LocalDate.now().plusDays(6),
            dueTime = LocalTime.of(15, 30),
            category = Category.WORK,
            priority = Priority.MEDIUM,
            reminder = Reminder.ONE_DAY_BEFORE,
            isCompleted = false
        )
    )

    val todayDeadlines: List<Deadline>
        get() = sampleDeadlines.filter { it.dueDate.isEqual(LocalDate.now()) }

    val tomorrowDeadlines: List<Deadline>
        get() = sampleDeadlines.filter { it.dueDate.isEqual(LocalDate.now().plusDays(1)) }

    val thisWeekDeadlines: List<Deadline>
        get() = sampleDeadlines.filter {
            val today = LocalDate.now()
            it.dueDate.isAfter(today.plusDays(1)) && it.dueDate.isBefore(today.plusDays(8))
        }
}
