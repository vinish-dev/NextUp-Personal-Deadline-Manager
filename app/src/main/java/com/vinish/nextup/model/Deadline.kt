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
    val subtasks: List<Subtask> = emptyList(),
    val isCompleted: Boolean = false
)

fun Deadline.isOverdue(
    nowDate: LocalDate = LocalDate.now(),
    nowTime: LocalTime = LocalTime.now()
): Boolean {
    if (isCompleted) return false
    return when {
        dueDate.isBefore(nowDate) -> true
        dueDate.isEqual(nowDate) -> dueTime != null && dueTime.isBefore(nowTime)
        else -> false
    }
}

private val COMMON_ACRONYMS = setOf(
    "dsa", "dbms", "sql", "cse", "ui/ux", "api", "ai", "ml", "os", "it", "pdf", "hr", "pr", "qa", "iot"
)

fun String.toTitleCase(): String {
    if (isBlank()) return this
    return split(" ").joinToString(" ") { word ->
        when {
            word.isEmpty() -> word
            word.lowercase(java.util.Locale.getDefault()) in COMMON_ACRONYMS -> word.uppercase(java.util.Locale.getDefault())
            word.length > 1 && word.all { !it.isLetter() || it.isUpperCase() } -> word
            else -> word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
            }
        }
    }
}

fun String.toSentenceCase(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return this
    val regex = Regex("""(^|[.!?]\s+)(\p{Ll})""")
    return regex.replace(trimmed) { matchResult ->
        val prefix = matchResult.groupValues[1]
        val letter = matchResult.groupValues[2]
        prefix + letter.uppercase(java.util.Locale.getDefault())
    }
}
