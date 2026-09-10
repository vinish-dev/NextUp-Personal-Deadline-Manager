package com.vinish.nextup.ui.categories.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.home.components.PriorityTag
import com.vinish.nextup.ui.home.components.formatDueDate
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderMedium
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.CheckboxChecked
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.SurfaceSubtle
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun CategoryItemCard(
    category: Category,
    totalCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    isExpanded: Boolean = false,
    showCompletedDeadlines: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleComplete: ((Deadline) -> Unit)? = null
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progress * 100).toInt()

    val pendingCount = (totalCount - completedCount).coerceAtLeast(0)
    val pendingText = if (pendingCount == 1) "1 pending" else "$pendingCount pending"
    val completedText = "$completedCount completed"
    val subtitleText = "$pendingText  •  $completedText"

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    val handleHeaderClick = onClick ?: onToggleExpand

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderStoke)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = handleHeaderClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(category.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.displayName(),
                        tint = category.iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.displayName(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse category" else "Expand category",
                            tint = TextSecondary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(arrowRotation)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = subtitleText,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = category.iconTint,
                            trackColor = SurfaceSubtle
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$percentage%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Expanded Tasks Section
            if (isExpanded) {
                val pendingDeadlines = remember(deadlines) { deadlines.filter { !it.isCompleted } }
                val completedDeadlines = remember(deadlines) { deadlines.filter { it.isCompleted } }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = BorderLight,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (deadlines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No deadlines in this category",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (!showCompletedDeadlines && pendingDeadlines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All caught up! No pending deadlines",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Pending Deadlines
                        pendingDeadlines.forEach { deadline ->
                            CategoryTaskItem(
                                deadline = deadline,
                                onClick = { onDeadlineClick?.invoke(deadline) },
                                onToggleComplete = { onToggleComplete?.invoke(deadline) }
                            )
                        }

                        // Completed Deadlines (only displayed when showCompletedDeadlines is ON)
                        if (showCompletedDeadlines && completedDeadlines.isNotEmpty()) {
                            completedDeadlines.forEach { deadline ->
                                CategoryTaskItem(
                                    deadline = deadline,
                                    onClick = { onDeadlineClick?.invoke(deadline) },
                                    onToggleComplete = { onToggleComplete?.invoke(deadline) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTaskItem(
    deadline: Deadline,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueDateInfo = formatDueDate(deadline.dueDate, deadline.dueTime)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceSubtle)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Complete Checkbox Circle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (deadline.isCompleted) {
                        Modifier.background(CheckboxChecked)
                    } else {
                        Modifier.border(1.5.dp, BorderMedium, CircleShape)
                    }
                )
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center
        ) {
            if (deadline.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Mark as incomplete",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Due Date Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deadline.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (deadline.isCompleted) TextTertiary else TextPrimary,
                textDecoration = if (deadline.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dueDateInfo.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (deadline.isCompleted) TextTertiary else if (dueDateInfo.isAlert) PriorityHighText else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Priority Badge
        PriorityTag(priority = deadline.priority)
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryItemCardPreview() {
    val sampleTasks = listOf(
        Deadline(
            id = 1,
            title = "Database Systems Project",
            dueDate = LocalDate.now(),
            dueTime = LocalTime.of(23, 59),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            isCompleted = false
        ),
        Deadline(
            id = 2,
            title = "Linear Algebra Assignment",
            dueDate = LocalDate.now().plusDays(2),
            category = Category.EDUCATION,
            priority = Priority.MEDIUM,
            isCompleted = true
        )
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryItemCard(
            category = Category.EDUCATION,
            totalCount = 2,
            completedCount = 1,
            deadlines = sampleTasks,
            isExpanded = true
        )
    }
}
