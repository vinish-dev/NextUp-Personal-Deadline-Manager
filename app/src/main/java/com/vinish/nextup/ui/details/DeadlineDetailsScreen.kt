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

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    onDeleteSubtask: ((Deadline, Subtask) -> Unit)? = null,
    onReschedule: ((Deadline, LocalDate) -> Unit)? = null,
    onShareClick: ((Deadline) -> Unit)? = null
) {
    val context = LocalContext.current
    val activeDeadline = initialDeadline
        ?: SampleDeadlines.sampleDeadlines.find { it.id == deadlineId }

    if (activeDeadline == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onBackClick()
        }
        return
    }

    val handleShare: () -> Unit = {
        if (onShareClick != null) {
            onShareClick.invoke(activeDeadline)
        } else {
            val dateStr = activeDeadline.dueDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH))
            val timeStr = activeDeadline.dueTime?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
            val dueStr = if (timeStr != null) "$dateStr at $timeStr" else dateStr
            val sb = StringBuilder()
            sb.append("📌 *${activeDeadline.title}*\n")
            if (!activeDeadline.description.isNullOrBlank()) {
                sb.append("${activeDeadline.description}\n\n")
            }
            sb.append("📅 Due: $dueStr\n")
            sb.append("🏷️ Category: ${activeDeadline.category.displayName} | Priority: ${activeDeadline.priority.name.lowercase().replaceFirstChar { it.uppercase() }}\n")
            if (activeDeadline.subtasks.isNotEmpty()) {
                val done = activeDeadline.subtasks.count { it.isCompleted }
                sb.append("\n📋 Subtasks ($done/${activeDeadline.subtasks.size}):\n")
                activeDeadline.subtasks.forEach { sub ->
                    sb.append("  ${if (sub.isCompleted) "✓" else "□"} ${sub.title}\n")
                }
            }
            sb.append("\nManaged with NextUp 🚀")

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Deadline"))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            DetailsTopBar(
                onBackClick = onBackClick,
                category = activeDeadline.category,
                onEditClick = { onEditClick?.invoke(activeDeadline) },
                onDeleteClick = { onDeleteClick?.invoke(activeDeadline) },
                onShareClick = handleShare
            )
        },
        bottomBar = {
            DeadlineBottomBar(
                isCompleted = activeDeadline.isCompleted,
                onToggleCompleted = { onToggleCompleted?.invoke(activeDeadline) }
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
            // Hero card: Category accent -> Urgency Countdown -> Title -> Description -> Postpone chips
            item {
                DeadlineHeroCard(
                    deadline = activeDeadline,
                    onToggleCompleted = { onToggleCompleted?.invoke(activeDeadline) },
                    onReschedule = { newDate -> onReschedule?.invoke(activeDeadline, newDate) }
                )
            }

            // Info Card: Due Date, Priority, Reminder, Repeat, and Calendar Sync
            item {
                DeadlineInfoCard(deadline = activeDeadline)
            }

            // Subtasks card: Progress bar, interactive list, delete item, and inline add
            item {
                DeadlineSubtasksCard(
                    subtasks = activeDeadline.subtasks,
                    onToggleSubtask = { toggledSubtask ->
                        onToggleSubtask?.invoke(activeDeadline, toggledSubtask)
                    },
                    onAddSubtask = { newTitle ->
                        onAddSubtask?.invoke(activeDeadline, newTitle)
                    },
                    onDeleteSubtask = { subtaskToDelete ->
                        onDeleteSubtask?.invoke(activeDeadline, subtaskToDelete)
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