package com.vinish.nextup.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderMedium
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityMediumText
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.SurfaceSubtle
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.runtime.Immutable
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DueDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val DueTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

/**
 * Holds formatted date/time text and alert indicator for a deadline.
 */
@Immutable
data class DueDateInfo(
    val text: String,
    val isAlert: Boolean,
    val isOverdue: Boolean
) {
    val formattedText: String get() = text
}

/**
 * Formats deadline date and time into a [DueDateInfo] object.
 */
fun formatDueDate(dueDate: LocalDate, dueTime: LocalTime?): DueDateInfo {
    val today = LocalDate.now()
    val isOverdue = dueDate.isBefore(today)
    val isToday = dueDate.isEqual(today)
    val isTomorrow = dueDate.isEqual(today.plusDays(1))

    val dateStr = when {
        isOverdue -> "Overdue"
        isToday -> "Today"
        isTomorrow -> "Tomorrow"
        else -> dueDate.format(DueDateFormatter)
    }

    val timeStr = dueTime?.format(DueTimeFormatter)
    val fullStr = if (timeStr != null) "$dateStr · $timeStr" else dateStr
    val isAlert = isOverdue || isToday

    return DueDateInfo(
        text = fullStr,
        isAlert = isAlert,
        isOverdue = isOverdue
    )
}

@Composable
fun DeadlineCard(
    deadline: Deadline,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onToggleCompleted: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    enableSwipe: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val dueDateInfo = remember(deadline.dueDate, deadline.dueTime) {
        formatDueDate(deadline.dueDate, deadline.dueTime)
    }
    val completedCount = remember(deadline.subtasks) {
        deadline.subtasks.count { it.isCompleted }
    }
    val totalSubtasks = deadline.subtasks.size

    val targetCheckboxBorderColor = remember(deadline.isCompleted, deadline.priority) {
        if (deadline.isCompleted) PrimaryBlue
        else when (deadline.priority) {
            Priority.HIGH -> PriorityHighText
            Priority.MEDIUM -> PriorityMediumText
            Priority.LOW -> PriorityLowText
        }
    }

    val checkboxBorderColor by animateColorAsState(
        targetValue = targetCheckboxBorderColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkboxBorder"
    )
    val checkboxBgColor by animateColorAsState(
        targetValue = if (deadline.isCompleted) PrimaryBlue else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkboxBg"
    )
    val checkboxScale by animateFloatAsState(
        targetValue = if (deadline.isCompleted) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkboxScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (deadline.isCompleted) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "contentAlpha"
    )

    val scope = rememberCoroutineScope()
    val wipeProgress = remember { Animatable(0f) }

    val handleToggleCompleted = {
        if (!deadline.isCompleted) {
            // Fast, punchy wipe across the card before state update
            scope.launch {
                wipeProgress.snapTo(0f)
                wipeProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                )
                onToggleCompleted?.invoke()
                delay(60)
                wipeProgress.snapTo(0f)
            }
        } else {
            onToggleCompleted?.invoke()
        }
    }

    val cardShape = remember { RoundedCornerShape(16.dp) }

    val cardContent = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .drawWithContent {
                    drawContent()
                    val progress = wipeProgress.value
                    if (progress > 0f && progress <= 1f) {
                        // Crisp completion wipe banner / sweep effect
                        val wipeWidth = size.width * progress
                        // Light primary blue wash with subtle gradient edge
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.16f),
                                    PrimaryBlue.copy(alpha = 0.28f),
                                    PrimaryBlue.copy(alpha = 0.45f)
                                ),
                                startX = 0f,
                                endX = wipeWidth
                            ),
                            topLeft = Offset.Zero,
                            size = Size(wipeWidth, size.height)
                        )
                        // Leading edge highlight line for crisp physical wipe look
                        drawRect(
                            color = PrimaryBlue.copy(alpha = 0.85f),
                            topLeft = Offset(wipeWidth - 3.dp.toPx().coerceAtLeast(0f), 0f),
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
                }
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                ),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (deadline.isCompleted) BorderLight.copy(alpha = 0.5f) else BorderLight
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tactile Circular Checkbox with Spring physics & Haptics
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkboxScale)
                        .clip(CircleShape)
                        .background(checkboxBgColor)
                        .border(1.75.dp, checkboxBorderColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            handleToggleCompleted()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (deadline.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Main Content: Title, metadata tags
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                ) {
                    Text(
                        text = deadline.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (deadline.isCompleted) TextTertiary else TextPrimary,
                        textDecoration = if (deadline.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category indicator dot + text
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(deadline.category.iconTint)
                        )
                        Text(
                            text = deadline.category.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )

                        // Due text
                        Text(
                            text = dueDateInfo.text,
                            fontSize = 12.sp,
                            fontWeight = if (dueDateInfo.isOverdue && !deadline.isCompleted) FontWeight.SemiBold else FontWeight.Medium,
                            color = when {
                                deadline.isCompleted -> TextTertiary
                                dueDateInfo.isOverdue -> PriorityHighText
                                dueDateInfo.isAlert -> PrimaryBlue
                                else -> TextSecondary
                            }
                        )

                        // Optional Subtask counter badge
                        if (totalSubtasks > 0) {
                            Text(
                                text = "·",
                                fontSize = 12.sp,
                                color = TextTertiary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Checklist,
                                    contentDescription = null,
                                    tint = if (completedCount == totalSubtasks) PrimaryBlue else TextTertiary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$completedCount/$totalSubtasks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (completedCount == totalSubtasks) PrimaryBlue else TextTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (!enableSwipe) {
        Box(modifier = modifier) {
            cardContent()
        }
    } else {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleCompleted?.invoke()
                        false // Snap back after toggling complete state
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        if (onDelete != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete.invoke()
                            true
                        } else {
                            false
                        }
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = onToggleCompleted != null,
            enableDismissFromEndToStart = onDelete != null,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd
                val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart

                val bgColor by animateColorAsState(
                    targetValue = when {
                        isStartToEnd -> if (deadline.isCompleted) PrimaryBlueLight else Color(0xFFDCFCE7)
                        isEndToStart -> Color(0xFFFEE2E2)
                        else -> Color.Transparent
                    },
                    label = "swipe_bg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (isStartToEnd) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Complete",
                                tint = if (deadline.isCompleted) PrimaryBlue else Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (deadline.isCompleted) "Reopen" else "Complete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deadline.isCompleted) PrimaryBlue else Color(0xFF16A34A)
                            )
                        }
                    } else if (isEndToStart) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Delete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            modifier = modifier
        ) {
            cardContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineCardPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DeadlineCard(
            deadline = Deadline(
                id = 1L,
                title = "DBMS Assignment",
                dueDate = LocalDate.now(),
                dueTime = LocalTime.of(23, 59),
                category = Category.EDUCATION,
                priority = Priority.HIGH,
                isCompleted = false
            )
        )
        DeadlineCard(
            deadline = Deadline(
                id = 2L,
                title = "College Application",
                dueDate = LocalDate.now().plusDays(1),
                dueTime = LocalTime.of(17, 0),
                category = Category.DOCUMENTS,
                priority = Priority.MEDIUM,
                isCompleted = true
            )
        )
    }
}

