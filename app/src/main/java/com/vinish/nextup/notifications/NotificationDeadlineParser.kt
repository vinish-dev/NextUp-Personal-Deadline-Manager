package com.vinish.nextup.notifications

import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Priority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.regex.Pattern

data class ParsedDeadlineInfo(
    val title: String,
    val description: String?,
    val dueDate: LocalDate,
    val dueTime: LocalTime?,
    val category: Category,
    val priority: Priority,
    val sourceApp: String?
)

object NotificationDeadlineParser {

    private val KEYWORD_PATTERN = Pattern.compile(
        "\\b(deadline|due|meeting|submit|submission|assignment|sync|call|quiz|exam|project|presentation|review|interview|reminder|event|appointment|webinar|schedule|scheduled)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val TIME_PATTERN = Pattern.compile(
        "(?:at|by|before)?\\s*\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val TIME_24H_PATTERN = Pattern.compile(
        "(?:at|by|before)\\s*\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val MONTH_NAMES = listOf(
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december"
    )

    private val MONTH_SHORT = listOf(
        "jan", "feb", "mar", "apr", "may", "jun",
        "jul", "aug", "sep", "oct", "nov", "dec"
    )

    fun parse(title: String?, text: String?, packageName: String?): ParsedDeadlineInfo? {
        val rawTitle = title?.trim().orEmpty()
        val rawText = text?.trim().orEmpty()
        val combined = "$rawTitle $rawText".trim()

        if (combined.isBlank()) return null

        val keywordMatcher = KEYWORD_PATTERN.matcher(combined)
        val hasKeyword = keywordMatcher.find()

        val parsedDate = extractDate(combined)
        val parsedTime = extractTime(combined)

        if (!hasKeyword && parsedDate == null && parsedTime == null) {
            return null
        }

        val dueDate = parsedDate ?: run {
            val now = LocalTime.now()
            if (parsedTime != null && parsedTime.isBefore(now)) {
                LocalDate.now().plusDays(1)
            } else {
                LocalDate.now()
            }
        }

        val category = inferCategory(packageName, combined)
        val priority = inferPriority(combined)
        val cleanTitle = buildCleanTitle(rawTitle, rawText)

        return ParsedDeadlineInfo(
            title = cleanTitle,
            description = if (rawText.isNotBlank() && rawText != cleanTitle) rawText else "Captured from notification",
            dueDate = dueDate,
            dueTime = parsedTime,
            category = category,
            priority = priority,
            sourceApp = packageName
        )
    }

    private fun extractDate(text: String): LocalDate? {
        val lower = text.lowercase(Locale.ENGLISH)
        val today = LocalDate.now()

        if (lower.contains("day after tomorrow")) {
            return today.plusDays(2)
        }
        if (lower.contains("tomorrow")) {
            return today.plusDays(1)
        }
        if (lower.contains("today") || lower.contains("tonight")) {
            return today
        }

        val dayOfWeekMap = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY
        )

        for ((dayName, dayEnum) in dayOfWeekMap) {
            val dayPattern = Pattern.compile("\\b(?:on|by|this|next)?\\s*$dayName\\b", Pattern.CASE_INSENSITIVE)
            if (dayPattern.matcher(text).find()) {
                var target = today.with(TemporalAdjusters.nextOrSame(dayEnum))
                if (target.isEqual(today) && !lower.contains("today")) {
                    target = today.with(TemporalAdjusters.next(dayEnum))
                }
                return target
            }
        }

        for (i in MONTH_NAMES.indices) {
            val fullName = MONTH_NAMES[i]
            val shortName = MONTH_SHORT[i]
            val monthNum = i + 1

            val pattern1 = Pattern.compile("\\b(?:$fullName|$shortName)\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b", Pattern.CASE_INSENSITIVE)
            val matcher1 = pattern1.matcher(text)
            if (matcher1.find()) {
                val day = matcher1.group(1)?.toIntOrNull()
                if (day != null && day in 1..31) {
                    val year = if (monthNum < today.monthValue) today.year + 1 else today.year
                    return runCatching { LocalDate.of(year, monthNum, day) }.getOrNull()
                }
            }

            val pattern2 = Pattern.compile("\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:$fullName|$shortName)\\b", Pattern.CASE_INSENSITIVE)
            val matcher2 = pattern2.matcher(text)
            if (matcher2.find()) {
                val day = matcher2.group(1)?.toIntOrNull()
                if (day != null && day in 1..31) {
                    val year = if (monthNum < today.monthValue) today.year + 1 else today.year
                    return runCatching { LocalDate.of(year, monthNum, day) }.getOrNull()
                }
            }
        }

        return null
    }

    private fun extractTime(text: String): LocalTime? {
        val matcher12h = TIME_PATTERN.matcher(text)
        if (matcher12h.find()) {
            val hourRaw = matcher12h.group(1)?.toIntOrNull() ?: return null
            val minuteRaw = matcher12h.group(2)?.toIntOrNull() ?: 0
            val amPm = matcher12h.group(3)?.lowercase(Locale.ENGLISH) ?: ""

            var hour = hourRaw
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0

            return runCatching { LocalTime.of(hour, minuteRaw) }.getOrNull()
        }

        val matcher24h = TIME_24H_PATTERN.matcher(text)
        if (matcher24h.find()) {
            val hour = matcher24h.group(1)?.toIntOrNull() ?: return null
            val minute = matcher24h.group(2)?.toIntOrNull() ?: 0
            return runCatching { LocalTime.of(hour, minute) }.getOrNull()
        }

        return null
    }

    private fun inferCategory(packageName: String?, text: String): Category {
        val lowerPkg = packageName?.lowercase(Locale.ENGLISH).orEmpty()
        val lowerText = text.lowercase(Locale.ENGLISH)

        return when {
            lowerPkg.contains("teams") || lowerPkg.contains("slack") || lowerPkg.contains("zoom") ||
            lowerPkg.contains("meet") || lowerText.contains("meeting") || lowerText.contains("sync") ||
            lowerText.contains("client") || lowerText.contains("work") -> Category.WORK

            lowerPkg.contains("canvas") || lowerPkg.contains("classroom") || lowerPkg.contains("blackboard") ||
            lowerPkg.contains("moodle") || lowerText.contains("assignment") || lowerText.contains("homework") ||
            lowerText.contains("quiz") || lowerText.contains("exam") || lowerText.contains("course") ||
            lowerText.contains("class") -> Category.EDUCATION

            lowerPkg.contains("pay") || lowerPkg.contains("bank") || lowerPkg.contains("wallet") ||
            lowerText.contains("bill") || lowerText.contains("invoice") || lowerText.contains("payment") ||
            lowerText.contains("recharge") -> Category.FINANCE

            lowerPkg.contains("health") || lowerPkg.contains("fit") || lowerText.contains("doctor") ||
            lowerText.contains("medicine") || lowerText.contains("hospital") || lowerText.contains("appointment") -> Category.PERSONAL

            lowerPkg.contains("flight") || lowerPkg.contains("irctc") || lowerPkg.contains("hotel") ||
            lowerPkg.contains("trip") || lowerText.contains("flight") || lowerText.contains("train") ||
            lowerText.contains("ticket") -> Category.OTHER

            lowerText.contains("subscription") || lowerText.contains("renew") || lowerText.contains("membership") -> Category.FINANCE
            lowerText.contains("passport") || lowerText.contains("license") || lowerText.contains("visa") -> Category.DOCUMENTS

            else -> Category.PERSONAL
        }
    }

    private fun inferPriority(text: String): Priority {
        val lower = text.lowercase(Locale.ENGLISH)
        return when {
            lower.contains("urgent") || lower.contains("asap") || lower.contains("crucial") ||
            lower.contains("important") || lower.contains("immediately") || lower.contains("final notice") -> Priority.HIGH

            lower.contains("medium") || lower.contains("meeting") || lower.contains("assignment") ||
            lower.contains("submission") || lower.contains("quiz") -> Priority.MEDIUM

            else -> Priority.LOW
        }
    }

    private fun buildCleanTitle(rawTitle: String, rawText: String): String {
        if (rawTitle.isNotBlank() && rawTitle.length in 4..60) {
            return rawTitle
        }

        if (rawText.isNotBlank()) {
            val lines = rawText.lines()
            val firstLine = lines.firstOrNull()?.trim() ?: ""
            if (firstLine.length in 5..65) {
                return firstLine
            }
            if (rawText.length <= 65) {
                return rawText
            }
            return rawText.take(62) + "..."
        }

        return if (rawTitle.isNotBlank()) rawTitle else "Scheduled Deadline"
    }
}
