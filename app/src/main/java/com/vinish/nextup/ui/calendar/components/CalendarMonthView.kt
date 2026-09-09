package com.vinish.nextup.ui.calendar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.SurfaceWhite
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

@Composable
fun CalendarMonthView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    deadlinesByDate: Map<LocalDate, List<Deadline>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    onPreviousMonthClick: () -> Unit = {},
    onNextMonthClick: () -> Unit = {},
    onTodayClick: () -> Unit = {}
) {
    val firstOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val leadingDays = firstOfMonth.dayOfWeek.value - 1
    val totalVisibleDays = leadingDays + daysInMonth
    val numRows = ceil(totalVisibleDays / 7.0).toInt()
    val startDate = firstOfMonth.minusDays(leadingDays.toLong())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderStoke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (showHeader) {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonthClick = onPreviousMonthClick,
                    onNextMonthClick = onNextMonthClick,
                    onTodayClick = onTodayClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            CalendarWeekHeader()

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val dayIndex = row * 7 + col
                            val cellDate = startDate.plusDays(dayIndex.toLong())
                            val isCurrentMonth = cellDate.month == currentMonth.month && cellDate.year == currentMonth.year
                            val isSelected = cellDate.isEqual(selectedDate)
                            val isToday = cellDate.isEqual(LocalDate.now())
                            val deadlines = deadlinesByDate[cellDate] ?: emptyList()

                            CalendarDayCell(
                                date = cellDate,
                                isSelected = isSelected,
                                isToday = isToday,
                                isCurrentMonth = isCurrentMonth,
                                deadlines = deadlines,
                                onDateClick = onDateClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarMonthViewPreview() {
    CalendarMonthView(
        currentMonth = YearMonth.now(),
        selectedDate = LocalDate.now(),
        deadlinesByDate = emptyMap(),
        onDateClick = {}
    )
}
