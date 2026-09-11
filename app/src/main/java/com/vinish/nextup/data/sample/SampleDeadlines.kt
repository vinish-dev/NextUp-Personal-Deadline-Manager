package com.vinish.nextup.data.sample

import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.model.Subtask
import java.time.LocalDate
import java.time.LocalTime

object SampleDeadlines {
    val sampleDeadlines = listOf(
        Deadline(
            id = 1L,
            title = "DBMS Assignment",
            description = "Write and submit the final report.",
            dueDate = LocalDate.now(),
            dueTime = LocalTime.of(23, 59),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            reminder = Reminder.SEVEN_DAYS_BEFORE,
            subtasks = listOf(
                Subtask(id = "1", title = "Complete normalization exercises", isCompleted = true),
                Subtask(id = "2", title = "Write SQL queries for chapter 5", isCompleted = true),
                Subtask(id = "3", title = "Draft and submit the final report", isCompleted = false)
            ),
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
        ),
        Deadline(
            id = 8L,
            title = "Semester Final Exams",
            description = "Prepare revision notes and syllabus coverage for finals",
            dueDate = LocalDate.now().plusDays(14),
            dueTime = LocalTime.of(9, 30),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            reminder = Reminder.SEVEN_DAYS_BEFORE,
            isCompleted = false
        )
    )

    val todayDeadlines: List<Deadline>
        get() = sampleDeadlines.filter { it.dueDate.isEqual(LocalDate.now()) }

    val tomorrowDeadlines: List<Deadline>
        get() = sampleDeadlines.filter { it.dueDate.isEqual(LocalDate.now().plusDays(1)) }

    val thisWeekDeadlines: List<Deadline>
        get() = sampleDeadlines.filter {
            val today = java.time.LocalDate.now()
            val tomorrow = today.plusDays(1)
            val endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
            it.dueDate.isAfter(tomorrow) && !it.dueDate.isAfter(endOfWeek)
        }

    val laterDeadlines: List<Deadline>
        get() = sampleDeadlines.filter {
            val today = java.time.LocalDate.now()
            val endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
            it.dueDate.isAfter(endOfWeek)
        }
}
