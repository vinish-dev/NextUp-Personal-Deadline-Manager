package com.vinish.nextup.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.PrimaryBlue

import com.vinish.nextup.model.isOverdue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onOverviewClick: ((OverviewType) -> Unit)? = null
) {
    val today = remember { LocalDate.now() }
    val nowTime = remember { LocalTime.now() }
    val tomorrow = remember(today) { today.plusDays(1) }
    val endOfWeek = remember(today) { today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) }

    val overdueDeadlines = remember(deadlines, today, nowTime) {
        deadlines.filter { it.isOverdue(today, nowTime) }
    }
    val todayDeadlines = remember(deadlines, today, nowTime) {
        deadlines.filter { it.dueDate.isEqual(today) && !it.isOverdue(today, nowTime) }
    }
    val tomorrowDeadlines = remember(deadlines, today, nowTime, tomorrow) {
        deadlines.filter { it.dueDate.isEqual(tomorrow) && !it.isOverdue(today, nowTime) }
    }
    val thisWeekDeadlines = remember(deadlines, today, nowTime, tomorrow, endOfWeek) {
        deadlines.filter {
            it.dueDate.isAfter(tomorrow) && !it.dueDate.isAfter(endOfWeek) && !it.isOverdue(today, nowTime)
        }
    }
    val laterDeadlines = remember(deadlines, overdueDeadlines, todayDeadlines, tomorrowDeadlines, thisWeekDeadlines) {
        deadlines.filter { deadline ->
            deadline !in overdueDeadlines &&
            deadline !in todayDeadlines &&
            deadline !in tomorrowDeadlines &&
            deadline !in thisWeekDeadlines &&
            !deadline.dueDate.isBefore(today)
        }.sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
    }

    val overdueCount = overdueDeadlines.size
    val todayCount = remember(todayDeadlines) {
        todayDeadlines.count { !it.isCompleted }
    }
    val tomorrowCount = remember(tomorrowDeadlines) {
        tomorrowDeadlines.count { !it.isCompleted }
    }
    val thisWeekCount = remember(thisWeekDeadlines) {
        thisWeekDeadlines.count { !it.isCompleted }
    }
    val allTasksCount = remember(deadlines) {
        deadlines.count { !it.isCompleted }
    }

    val hasTasks = remember(overdueDeadlines, todayDeadlines, tomorrowDeadlines, thisWeekDeadlines, laterDeadlines) {
        overdueDeadlines.isNotEmpty() || todayDeadlines.isNotEmpty() || tomorrowDeadlines.isNotEmpty() || thisWeekDeadlines.isNotEmpty() || laterDeadlines.isNotEmpty()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 88.dp)
        ) {
            item {
                GreetingSection(
                    name = "Vinish",
                    onAvatarClick = onAvatarClick
                )
            }

            item {
                OverviewSection(
                    modifier = Modifier.padding(vertical = 8.dp),
                    overviewItems = listOf(
                        OverviewItem(OverviewType.OVERDUE, overdueCount),
                        OverviewItem(OverviewType.TODAY, todayCount),
                        OverviewItem(OverviewType.TOMORROW, tomorrowCount),
                        OverviewItem(OverviewType.ALL_TASKS, allTasksCount)
                    ),
                    onItemClick = onOverviewClick
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

            if (laterDeadlines.isNotEmpty()) {
                item {
                    DeadlineSection(
                        title = "Later",
                        deadlines = laterDeadlines,
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

        if (onAddDeadlineClick != null) {
            FloatingActionButton(
                onClick = onAddDeadlineClick,
                shape = CircleShape,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Deadline",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
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