package com.vinish.nextup.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.home.components.DeadlineSection
import com.vinish.nextup.ui.home.components.GreetingSection
import com.vinish.nextup.ui.home.components.OverviewSection
import com.vinish.nextup.ui.home.model.OverviewItem
import com.vinish.nextup.ui.home.components.HomeEmptyState
import com.vinish.nextup.ui.home.model.OverviewType

import com.vinish.nextup.model.isOverdue
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (() -> Unit)? = null
) {
    val today = LocalDate.now()
    val nowTime = LocalTime.now()

    val overdueDeadlines = remember(deadlines) {
        deadlines.filter { it.isOverdue(today, nowTime) }
    }
    val todayDeadlines = remember(deadlines) {
        deadlines.filter { it.dueDate.isEqual(today) && !it.isOverdue(today, nowTime) }
    }
    val tomorrowDeadlines = remember(deadlines) {
        deadlines.filter { it.dueDate.isEqual(today.plusDays(1)) }
    }
    val thisWeekDeadlines = remember(deadlines) {
        deadlines.filter {
            it.dueDate.isAfter(today.plusDays(1)) && it.dueDate.isBefore(today.plusDays(8))
        }
    }

    val overdueCount = overdueDeadlines.size
    val todayCount = remember(deadlines) {
        deadlines.count { !it.isCompleted && it.dueDate.isEqual(today) && !it.isOverdue(today, nowTime) }
    }
    val tomorrowCount = remember(deadlines) {
        deadlines.count { !it.isCompleted && it.dueDate.isEqual(today.plusDays(1)) }
    }
    val thisWeekCount = remember(deadlines) {
        deadlines.count { !it.isCompleted && it.dueDate.isAfter(today.plusDays(1)) && it.dueDate.isBefore(today.plusDays(8)) }
    }

    val hasTasks = remember(overdueDeadlines, todayDeadlines, tomorrowDeadlines, thisWeekDeadlines) {
        overdueDeadlines.isNotEmpty() || todayDeadlines.isNotEmpty() || tomorrowDeadlines.isNotEmpty() || thisWeekDeadlines.isNotEmpty()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        item {
            GreetingSection(
                name = "Vinish"
            )
        }

        item {
            OverviewSection(
                modifier = Modifier.padding(vertical = 8.dp),
                overviewItems = listOf(
                    OverviewItem(OverviewType.OVERDUE, overdueCount),
                    OverviewItem(OverviewType.TODAY, todayCount),
                    OverviewItem(OverviewType.TOMORROW, tomorrowCount),
                    OverviewItem(OverviewType.THIS_WEEK, thisWeekCount)
                )
            )
        }

        if (overdueDeadlines.isNotEmpty()) {
            item {
                DeadlineSection(
                    title = "Overdue",
                    deadlines = overdueDeadlines,
                    onDeadlineClick = onDeadlineClick
                )
            }
        }

        if (todayDeadlines.isNotEmpty()) {
            item {
                DeadlineSection(
                    title = "Today",
                    deadlines = todayDeadlines,
                    onDeadlineClick = onDeadlineClick
                )
            }
        }

        if (tomorrowDeadlines.isNotEmpty()) {
            item {
                DeadlineSection(
                    title = "Tomorrow",
                    deadlines = tomorrowDeadlines,
                    showSeeAll = false,
                    onDeadlineClick = onDeadlineClick
                )
            }
        }

        if (thisWeekDeadlines.isNotEmpty()) {
            item {
                DeadlineSection(
                    title = "This Week",
                    deadlines = thisWeekDeadlines,
                    onDeadlineClick = onDeadlineClick
                )
            }
        }

        if (!hasTasks) {
            item {
                HomeEmptyState(
                    onAddDeadlineClick = onAddDeadlineClick
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}