package com.vinish.nextup.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Subtask
import com.vinish.nextup.ui.details.components.DeadlineBottomBar
import com.vinish.nextup.ui.details.components.DeadlineHeroCard
import com.vinish.nextup.ui.details.components.DeadlineInfoCard
import com.vinish.nextup.ui.details.components.DeadlineSubtasksCard
import com.vinish.nextup.ui.details.components.DetailsTopBar
import com.vinish.nextup.ui.theme.BackgroundLight

@Composable
fun DeadlineDetailsScreen(
    modifier: Modifier = Modifier,
    deadlineId: Long = 1L,
    initialDeadline: Deadline? = null,
    onBackClick: () -> Unit = {},
    onEditClick: ((Deadline) -> Unit)? = null,
    onDeleteClick: ((Deadline) -> Unit)? = null,
    onShareClick: ((Deadline) -> Unit)? = null
) {
    val deadline = initialDeadline ?: SampleDeadlines.sampleDeadlines.find { it.id == deadlineId }
        ?: SampleDeadlines.sampleDeadlines.first()

    var isCompleted by remember(deadline.id) { mutableStateOf(deadline.isCompleted) }
    var subtasks by remember(deadline.id) { mutableStateOf(deadline.subtasks) }

    val activeDeadline = deadline.copy(
        isCompleted = isCompleted,
        subtasks = subtasks
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            DetailsTopBar(
                onBackClick = onBackClick,
                onEditClick = { onEditClick?.invoke(activeDeadline) },
                onDeleteClick = { onDeleteClick?.invoke(activeDeadline) },
                onShareClick = { onShareClick?.invoke(activeDeadline) }
            )
        },
        bottomBar = {
            DeadlineBottomBar(
                isCompleted = isCompleted,
                onToggleCompleted = { isCompleted = !isCompleted },
                onEditClick = { onEditClick?.invoke(activeDeadline) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Hero card: Category icon -> Title -> Description -> Status / Priority
            item {
                DeadlineHeroCard(deadline = activeDeadline)
            }

            // Info Card: Due Date, Reminder, Repeat
            item {
                DeadlineInfoCard(deadline = activeDeadline)
            }

            // Subtasks card: Progress bar, interactive list, and inline add
            item {
                DeadlineSubtasksCard(
                    subtasks = subtasks,
                    onToggleSubtask = { toggledSubtask ->
                        subtasks = subtasks.map { subtask ->
                            if (subtask.id == toggledSubtask.id) {
                                subtask.copy(isCompleted = !subtask.isCompleted)
                            } else {
                                subtask
                            }
                        }
                    },
                    onAddSubtask = { newTitle ->
                        subtasks = subtasks + Subtask(title = newTitle)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DeadlineDetailsScreenPreview() {
    DeadlineDetailsScreen()
}