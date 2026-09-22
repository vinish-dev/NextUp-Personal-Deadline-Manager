package com.vinish.nextup.ui.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader
import com.vinish.nextup.ui.home.components.DeadlineCard
import com.vinish.nextup.ui.home.components.HomeEmptyState
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary

@Composable
fun AllTasksScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleComplete: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (() -> Unit)? = null,
    onEditDeadline: ((Deadline) -> Unit)? = null
) {
    val pendingDeadlines = remember(deadlines) {
        deadlines
            .filter { !it.isCompleted }
            .sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
    }

    val completedDeadlines = remember(deadlines) {
        deadlines
            .filter { it.isCompleted }
            .sortedWith(compareByDescending<Deadline> { it.dueDate }.thenByDescending { it.dueTime })
    }

    val pendingCount = pendingDeadlines.size
    val completedCount = completedDeadlines.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "All Tasks",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View and manage all your deadlines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }

            if (deadlines.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HomeEmptyState(
                        title = "No Tasks Found",
                        subtitle = "Create your first deadline to get started tracking your work.",
                        onAddDeadlineClick = onAddDeadlineClick
                    )
                }
            } else {
                // Pending Deadlines Section
                if (pendingDeadlines.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = if (completedDeadlines.isNotEmpty()) "Pending ($pendingCount)" else "Tasks ($pendingCount)",
                            showViewAll = false
                        )
                    }

                    items(pendingDeadlines, key = { it.id }) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            onClick = { onDeadlineClick?.invoke(deadline) },
                            onToggleComplete = { onToggleComplete?.invoke(deadline) },
                            onEdit = { onEditDeadline?.invoke(deadline) }
                        )
                    }
                }

                // Completed Deadlines Section (Clearly secondary, visually quieter, at the bottom)
                if (completedDeadlines.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        SectionHeader(
                            title = "Completed ($completedCount)",
                            showViewAll = false
                        )
                    }

                    items(completedDeadlines, key = { it.id }) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            onClick = { onDeadlineClick?.invoke(deadline) },
                            onToggleComplete = { onToggleComplete?.invoke(deadline) }
                        )
                    }
                }
            }
        }

        // Add Deadline FAB
        if (onAddDeadlineClick != null) {
            com.vinish.nextup.ui.components.AddDeadlineFab(
                onClick = onAddDeadlineClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun AllTasksScreenPreview() {
    AllTasksScreen(
        deadlines = SampleDeadlines.sampleDeadlines
    )
}
