package com.vinish.nextup.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader
import com.vinish.nextup.ui.home.components.DeadlineCard
import com.vinish.nextup.ui.home.components.DeadlineSection
import com.vinish.nextup.ui.home.components.HomeEmptyState
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderMedium
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HomeFilter(val title: String) {
    ALL("All"),
    OVERDUE("Overdue"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Done")
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = SampleDeadlines.sampleDeadlines,
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleCompleted: ((Deadline) -> Unit)? = null,
    onDeleteDeadline: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null
) {
    val today = remember { LocalDate.now() }
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }
    var showCompletedSection by remember { mutableStateOf(false) }

    val overdueDeadlines = remember(deadlines) {
        deadlines.filter { !it.isCompleted && it.dueDate.isBefore(today) }
    }
    val todayDeadlines = remember(deadlines) {
        deadlines.filter { !it.isCompleted && it.dueDate.isEqual(today) }
    }
    val tomorrowDeadlines = remember(deadlines) {
        deadlines.filter { !it.isCompleted && it.dueDate.isEqual(today.plusDays(1)) }
    }
    val thisWeekDeadlines = remember(deadlines) {
        deadlines.filter {
            !it.isCompleted && it.dueDate.isAfter(today.plusDays(1)) && it.dueDate.isBefore(today.plusDays(8))
        }
    }
    val laterDeadlines = remember(deadlines) {
        deadlines.filter {
            !it.isCompleted && it.dueDate.isAfter(today.plusDays(7))
        }
    }
    val upcomingAllDeadlines = remember(deadlines) {
        deadlines.filter { !it.isCompleted && it.dueDate.isAfter(today) }
    }
    val completedDeadlines = remember(deadlines) {
        deadlines.filter { it.isCompleted }
    }

    val pendingCount = remember(deadlines) { deadlines.count { !it.isCompleted } }
    val completedCount = completedDeadlines.size

    val hasUpcomingTasks = remember(todayDeadlines, tomorrowDeadlines, thisWeekDeadlines, laterDeadlines) {
        todayDeadlines.isNotEmpty() || tomorrowDeadlines.isNotEmpty() || thisWeekDeadlines.isNotEmpty() || laterDeadlines.isNotEmpty()
    }

    val formattedDateHeader = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)).uppercase()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        // Editorial Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDateHeader,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "My Deadlines",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$pendingCount pending · $completedCount completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Avatar / Profile Shortcut
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlueLight)
                        .clickable { onAvatarClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = PrimaryBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Smart Filter Pills (Things 3 / Apple Reminders style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val filterCount = when (filter) {
                        HomeFilter.ALL -> pendingCount
                        HomeFilter.OVERDUE -> overdueDeadlines.size
                        HomeFilter.TODAY -> todayDeadlines.size
                        HomeFilter.UPCOMING -> upcomingAllDeadlines.size
                        HomeFilter.COMPLETED -> completedCount
                    }
                    val isOverdueFilterWithItems = filter == HomeFilter.OVERDUE && filterCount > 0

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isSelected && isOverdueFilterWithItems -> PriorityHighText
                            isSelected -> PrimaryBlue
                            isOverdueFilterWithItems -> PriorityHighBg
                            else -> SurfaceWhite
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = when {
                                isSelected -> Color.Transparent
                                isOverdueFilterWithItems -> PriorityHighText.copy(alpha = 0.3f)
                                else -> BorderLight
                            }
                        ),
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = filter.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isSelected -> Color.White
                                    isOverdueFilterWithItems -> PriorityHighText
                                    else -> TextPrimary
                                }
                            )

                            // Count indicator badge
                            if (filterCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color.White.copy(alpha = 0.25f)
                                                isOverdueFilterWithItems -> PriorityHighText.copy(alpha = 0.15f)
                                                else -> PrimaryBlueLight
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterCount.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isSelected -> Color.White
                                            isOverdueFilterWithItems -> PriorityHighText
                                            else -> PrimaryBlue
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Content based on selected filter
        when (selectedFilter) {
            HomeFilter.ALL -> {
                if (overdueDeadlines.isNotEmpty()) {
                    item {
                        DeadlineSection(
                            title = "Overdue",
                            deadlines = overdueDeadlines,
                            onDeadlineClick = onDeadlineClick,
                            onToggleCompleted = onToggleCompleted,
                            onDeleteDeadline = onDeleteDeadline
                        )
                    }
                }

                if (todayDeadlines.isNotEmpty()) {
                    item {
                        DeadlineSection(
                            title = "Today",
                            deadlines = todayDeadlines,
                            onDeadlineClick = onDeadlineClick,
                            onToggleCompleted = onToggleCompleted,
                            onDeleteDeadline = onDeleteDeadline
                        )
                    }
                }

                if (tomorrowDeadlines.isNotEmpty()) {
                    item {
                        DeadlineSection(
                            title = "Tomorrow",
                            deadlines = tomorrowDeadlines,
                            onDeadlineClick = onDeadlineClick,
                            onToggleCompleted = onToggleCompleted,
                            onDeleteDeadline = onDeleteDeadline
                        )
                    }
                }

                if (thisWeekDeadlines.isNotEmpty()) {
                    item {
                        DeadlineSection(
                            title = "This Week",
                            deadlines = thisWeekDeadlines,
                            onDeadlineClick = onDeadlineClick,
                            onToggleCompleted = onToggleCompleted,
                            onDeleteDeadline = onDeleteDeadline
                        )
                    }
                }

                if (laterDeadlines.isNotEmpty()) {
                    item {
                        DeadlineSection(
                            title = "Later",
                            deadlines = laterDeadlines,
                            onDeadlineClick = onDeadlineClick,
                            onToggleCompleted = onToggleCompleted,
                            onDeleteDeadline = onDeleteDeadline
                        )
                    }
                }

                if (!hasUpcomingTasks && overdueDeadlines.isEmpty()) {
                    item {
                        HomeEmptyState(
                            onAddDeadlineClick = onAddDeadlineClick
                        )
                    }
                }

                // Collapsible Completed Section at bottom
                if (completedDeadlines.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCompletedSection = !showCompletedSection }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Completed (${completedDeadlines.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = if (showCompletedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }

                    if (showCompletedSection) {
                        items(
                            items = completedDeadlines,
                            key = { it.id }
                        ) { deadline ->
                            DeadlineCard(
                                deadline = deadline,
                                modifier = Modifier.animateItem(),
                                onClick = onDeadlineClick?.let { { it(deadline) } },
                                onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                                onDelete = onDeleteDeadline?.let { { it(deadline) } }
                            )
                        }
                    }
                }
            }

            HomeFilter.OVERDUE -> {
                if (overdueDeadlines.isNotEmpty()) {
                    items(
                        items = overdueDeadlines,
                        key = { it.id }
                    ) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            modifier = Modifier.animateItem(),
                            onClick = onDeadlineClick?.let { { it(deadline) } },
                            onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                            onDelete = onDeleteDeadline?.let { { it(deadline) } }
                        )
                    }
                } else {
                    item {
                        HomeEmptyState(
                            title = "No Overdue Tasks",
                            subtitle = "Great job! You're completely caught up on your deadlines.",
                            onAddDeadlineClick = onAddDeadlineClick
                        )
                    }
                }
            }

            HomeFilter.TODAY -> {
                if (todayDeadlines.isNotEmpty()) {
                    items(
                        items = todayDeadlines,
                        key = { it.id }
                    ) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            modifier = Modifier.animateItem(),
                            onClick = onDeadlineClick?.let { { it(deadline) } },
                            onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                            onDelete = onDeleteDeadline?.let { { it(deadline) } }
                        )
                    }
                } else {
                    item {
                        HomeEmptyState(
                            title = "Clear for Today",
                            subtitle = "No deadlines due today. Relax or get a head start on tomorrow!",
                            onAddDeadlineClick = onAddDeadlineClick
                        )
                    }
                }
            }

            HomeFilter.UPCOMING -> {
                if (upcomingAllDeadlines.isNotEmpty()) {
                    items(
                        items = upcomingAllDeadlines,
                        key = { it.id }
                    ) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            modifier = Modifier.animateItem(),
                            onClick = onDeadlineClick?.let { { it(deadline) } },
                            onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                            onDelete = onDeleteDeadline?.let { { it(deadline) } }
                        )
                    }
                } else {
                    item {
                        HomeEmptyState(
                            title = "No Upcoming Deadlines",
                            subtitle = "You have no upcoming tasks scheduled in the coming days.",
                            onAddDeadlineClick = onAddDeadlineClick
                        )
                    }
                }
            }

            HomeFilter.COMPLETED -> {
                if (completedDeadlines.isNotEmpty()) {
                    items(
                        items = completedDeadlines,
                        key = { it.id }
                    ) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            modifier = Modifier.animateItem(),
                            onClick = onDeadlineClick?.let { { it(deadline) } },
                            onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                            onDelete = onDeleteDeadline?.let { { it(deadline) } }
                        )
                    }
                } else {
                    item {
                        HomeEmptyState(
                            title = "No Completed Tasks Yet",
                            subtitle = "Check off tasks as you finish them to track your accomplishments!",
                            onAddDeadlineClick = onAddDeadlineClick
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}