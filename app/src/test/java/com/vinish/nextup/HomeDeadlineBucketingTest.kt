package com.vinish.nextup

import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.isOverdue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HomeDeadlineBucketingTest {

    @Test
    fun `sample deadlines include Later section with deadlines beyond 7 days`() {
        val later = SampleDeadlines.laterDeadlines
        assertTrue("Later deadlines should contain items due beyond 7 days", later.isNotEmpty())
        assertTrue("Item should be Semester Final Exams", later.any { it.title == "Semester Final Exams" })
    }

    @Test
    fun `deadlines due 8 or more days from today belong to later deadlines`() {
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        val deadlineIn10Days = Deadline(
            id = 99L,
            title = "Final Project Defense",
            dueDate = today.plusDays(10),
            category = Category.EDUCATION,
            priority = Priority.HIGH
        )

        val isOverdue = deadlineIn10Days.isOverdue(today, nowTime)
        val isToday = deadlineIn10Days.dueDate.isEqual(today) && !isOverdue
        val isTomorrow = deadlineIn10Days.dueDate.isEqual(today.plusDays(1))
        val isThisWeek = deadlineIn10Days.dueDate.isAfter(today.plusDays(1)) && deadlineIn10Days.dueDate.isBefore(today.plusDays(8))
        val isLater = !isOverdue && !isToday && !isTomorrow && !isThisWeek && !deadlineIn10Days.dueDate.isBefore(today)

        assertEquals(false, isOverdue)
        assertEquals(false, isToday)
        assertEquals(false, isTomorrow)
        assertEquals(false, isThisWeek)
        assertEquals(true, isLater)
    }
}
