package com.vinish.nextup.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.calendar.components.CalendarHeader
import com.vinish.nextup.ui.calendar.components.CalendarMonthView
import com.vinish.nextup.ui.calendar.components.CalendarScheduleSection
import com.vinish.nextup.ui.theme.BackgroundLight
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = SampleDeadlines.sampleDeadlines,
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleCompleted: ((Deadline) -> Unit)? = null,
    onDeleteDeadline: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (LocalDate) -> Unit = {}
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    val deadlinesByDate by remember(deadlines) {
        derivedStateOf { deadlines.groupBy { it.dueDate } }
    }

    val selectedDayDeadlines by remember(selectedDate, deadlinesByDate) {
        derivedStateOf { deadlinesByDate[selectedDate] ?: emptyList() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            // Month-Year in the middle with left/right arrows on the extreme ends
            item {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonthClick = {
                        currentMonth = currentMonth.minusMonths(1)
                    },
                    onNextMonthClick = {
                        currentMonth = currentMonth.plusMonths(1)
                    },
                    onTodayClick = {
                        val today = LocalDate.now()
                        selectedDate = today
                        currentMonth = YearMonth.now()
                    }
                )
            }

            // Calendar Month Grid Card
            item {
                CalendarMonthView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    deadlinesByDate = deadlinesByDate,
                    showHeader = false,
                    onDateClick = { date ->
                        selectedDate = date
                        if (YearMonth.from(date) != currentMonth) {
                            currentMonth = YearMonth.from(date)
                        }
                    }
                )
            }

            // Selected Date Schedule Section (with contextual Add action)
            item {
                CalendarScheduleSection(
                    selectedDate = selectedDate,
                    deadlines = selectedDayDeadlines,
                    onDeadlineClick = onDeadlineClick,
                    onToggleCompleted = onToggleCompleted,
                    onDeleteDeadline = onDeleteDeadline,
                    onAddDeadlineClick = { onAddDeadlineClick(selectedDate) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CalendarScreenPreview() {
    CalendarScreen()
}