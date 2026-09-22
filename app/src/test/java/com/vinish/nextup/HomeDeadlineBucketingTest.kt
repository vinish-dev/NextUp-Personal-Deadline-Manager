package com.vinish.nextup

import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.isOverdue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class HomeDeadlineBucketingTest {

    private fun bucketDeadlines(today: LocalDate, nowTime: LocalTime, deadlines: List<Deadline>): Map<String, List<Deadline>> {
        val tomorrow = today.plusDays(1)
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val overdue = deadlines.filter { it.isOverdue(today, nowTime) }
        val todayList = deadlines.filter { it.dueDate.isEqual(today) && !it.isOverdue(today, nowTime) }
        val tomorrowList = deadlines.filter { it.dueDate.isEqual(tomorrow) && !it.isOverdue(today, nowTime) }
        val thisWeek = deadlines.filter {
            it.dueDate.isAfter(tomorrow) && !it.dueDate.isAfter(endOfWeek) && !it.isOverdue(today, nowTime)
        }
        val later = deadlines.filter { deadline ->
            deadline !in overdue &&
            deadline !in todayList &&
            deadline !in tomorrowList &&
            deadline !in thisWeek &&
            !deadline.dueDate.isBefore(today)
        }.sortedWith(compareBy({ it.dueDate }, { it.dueTime }))

        return mapOf(
            "overdue" to overdue,
            "today" to todayList,
            "tomorrow" to tomorrowList,
            "thisWeek" to thisWeek,
            "later" to later
        )
    }

    @Test
    fun `Thursday scenario - This Week contains Saturday and Sunday, next Monday falls into Later`() {
        // Assume today is Thursday, 2026-09-10
        val thursday = LocalDate.of(2026, 9, 10)
        assertEquals(DayOfWeek.THURSDAY, thursday.dayOfWeek)
        val noon = LocalTime.of(12, 0)

        val dToday = Deadline(id = 1L, title = "Due Thursday", dueDate = thursday, category = Category.EDUCATION, priority = Priority.HIGH)
        val dTomorrow = Deadline(id = 2L, title = "Due Friday", dueDate = thursday.plusDays(1), category = Category.EDUCATION, priority = Priority.HIGH)
        val dSaturday = Deadline(id = 3L, title = "Due Saturday", dueDate = thursday.plusDays(2), category = Category.EDUCATION, priority = Priority.MEDIUM)
        val dSunday = Deadline(id = 4L, title = "Due Sunday", dueDate = thursday.plusDays(3), category = Category.EDUCATION, priority = Priority.LOW)
        val dNextMonday = Deadline(id = 5L, title = "Due Next Monday", dueDate = thursday.plusDays(4), category = Category.WORK, priority = Priority.HIGH)
        val dInTwoWeeks = Deadline(id = 6L, title = "Due In Two Weeks", dueDate = thursday.plusDays(14), category = Category.FINANCE, priority = Priority.LOW)

        val buckets = bucketDeadlines(
            today = thursday,
            nowTime = noon,
            deadlines = listOf(dToday, dTomorrow, dSaturday, dSunday, dNextMonday, dInTwoWeeks)
        )

        assertEquals(listOf(dToday), buckets["today"])
        assertEquals(listOf(dTomorrow), buckets["tomorrow"])
        // "This Week" strictly contains remaining days of current calendar week: Saturday & Sunday
        assertEquals(listOf(dSaturday, dSunday), buckets["thisWeek"])
        // "Later" naturally catches everything after the current calendar week
        assertEquals(listOf(dNextMonday, dInTwoWeeks), buckets["later"])
    }

    @Test
    fun `Saturday scenario - tomorrow is Sunday and This Week has no extra days, next Monday goes to Later`() {
        val saturday = LocalDate.of(2026, 9, 12)
        assertEquals(DayOfWeek.SATURDAY, saturday.dayOfWeek)
        val noon = LocalTime.of(12, 0)

        val dSunday = Deadline(id = 1L, title = "Due Sunday", dueDate = saturday.plusDays(1), category = Category.EDUCATION, priority = Priority.HIGH)
        val dNextMonday = Deadline(id = 2L, title = "Due Next Monday", dueDate = saturday.plusDays(2), category = Category.WORK, priority = Priority.MEDIUM)

        val buckets = bucketDeadlines(
            today = saturday,
            nowTime = noon,
            deadlines = listOf(dSunday, dNextMonday)
        )

        assertEquals(listOf(dSunday), buckets["tomorrow"])
        assertTrue("This Week should be empty on Saturday since Sunday is Tomorrow", buckets["thisWeek"]!!.isEmpty())
        assertEquals(listOf(dNextMonday), buckets["later"])
    }

    @Test
    fun `sample deadlines include Later section with items beyond current week`() {
        val later = SampleDeadlines.laterDeadlines
        assertTrue("Later deadlines should contain items beyond current week", later.isNotEmpty())
        assertTrue("Item should be Semester Final Exams", later.any { it.title == "Semester Final Exams" })
    }
}
