package com.vinish.nextup.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Subtask
import com.vinish.nextup.ui.details.components.DeadlineBottomBar
import com.vinish.nextup.ui.details.components.DeadlineHeroCard
import com.vinish.nextup.ui.details.components.DeadlineInfoCard
import com.vinish.nextup.ui.details.components.DeadlineSubtasksCard
import com.vinish.nextup.ui.details.components.DetailsTopBar
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary

@Composable
fun DeadlineDetailsScreen(
    modifier: Modifier = Modifier,
    deadlineId: Long = 1L,
    initialDeadline: Deadline? = null,
    onBackClick: () -> Unit = {},
    onEditClick: ((Deadline) -> Unit)? = null,
    onDeleteClick: ((Deadline) -> Unit)? = null,
    onToggleCompleted: ((Deadline) -> Unit)? = null,
    onToggleSubtask: ((Deadline, Subtask) -> Unit)? = null,
    onAddSubtask: ((Deadline, String) -> Unit)? = null,
    onShareClick: ((Deadline) -> Unit)? = null
) {
    val activeDeadline = initialDeadline ?: return
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Deadline?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this deadline? This action cannot be undone.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick?.invoke(activeDeadline)
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = PriorityHighText,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DetailsTopBar(
                onBackClick = onBackClick,
                isCompleted = activeDeadline.isCompleted,
                onEditClick = { onEditClick?.invoke(activeDeadline) },
                onDeleteClick = { showDeleteConfirmation = true },
                onShareClick = { onShareClick?.invoke(activeDeadline) }
            )
        },
        bottomBar = {
            DeadlineBottomBar(
                isCompleted = activeDeadline.isCompleted,
                onToggleCompleted = { onToggleCompleted?.invoke(activeDeadline) },
                onEditClick = { onEditClick?.invoke(activeDeadline) },
                onDeleteClick = { showDeleteConfirmation = true }
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
                    subtasks = activeDeadline.subtasks,
                    onToggleSubtask = { toggledSubtask ->
                        onToggleSubtask?.invoke(activeDeadline, toggledSubtask)
                    },
                    onAddSubtask = { newTitle ->
                        onAddSubtask?.invoke(activeDeadline, newTitle)
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