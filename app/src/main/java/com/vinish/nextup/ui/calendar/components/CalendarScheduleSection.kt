package com.vinish.nextup.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader
import com.vinish.nextup.ui.home.components.DeadlineCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScheduleSection(
    selectedDate: LocalDate,
    deadlines: List<Deadline>,
    modifier: Modifier = Modifier,
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val sectionTitle = when {
        selectedDate.isEqual(today) -> "Today, ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))}"
        selectedDate.isEqual(tomorrow) -> "Tomorrow, ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))}"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH))
    }

    val countText = when (deadlines.size) {
        0 -> ""
        1 -> "1 deadline"
        else -> "${deadlines.size} deadlines"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = sectionTitle,
            showViewAll = deadlines.isNotEmpty(),
            actionText = countText,
            onViewAllClick = { onSeeAllClick?.invoke() }
        )

        if (deadlines.isEmpty()) {
            CalendarEmptyState(
                title = "No Deadlines",
                subtitle = "Nothing scheduled for this date. Relax or plan ahead!"
            )
        } else {
            deadlines.forEach { deadline ->
                DeadlineCard(
                    deadline = deadline,
                    onClick = onDeadlineClick?.let { { it(deadline) } }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScheduleSectionPreview() {
    CalendarScheduleSection(
        selectedDate = LocalDate.now(),
        deadlines = SampleDeadlines.todayDeadlines
    )
}
