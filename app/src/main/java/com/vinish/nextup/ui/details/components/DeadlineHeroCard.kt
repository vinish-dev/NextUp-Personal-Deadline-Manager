package com.vinish.nextup.ui.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.add.components.displayName
import com.vinish.nextup.ui.theme.AlertBannerBg
import com.vinish.nextup.ui.theme.AlertBannerText
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderMedium
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityMediumBg
import com.vinish.nextup.ui.theme.PriorityMediumText
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun DeadlineHeroCard(
    deadline: Deadline,
    modifier: Modifier = Modifier,
    onToggleCompleted: (() -> Unit)? = null,
    onReschedule: ((LocalDate) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val today = LocalDate.now()
    val isOverdue = deadline.dueDate.isBefore(today) && !deadline.isCompleted
    val isToday = deadline.dueDate.isEqual(today) && !deadline.isCompleted
    val daysDiff = ChronoUnit.DAYS.between(today, deadline.dueDate)

    val (statusText, statusBg, statusColor) = when {
        deadline.isCompleted -> Triple("✓ Completed", PriorityLowBg, PriorityLowText)
        isOverdue -> {
            val overdueDays = ChronoUnit.DAYS.between(deadline.dueDate, today)
            val label = if (overdueDays <= 1) "⚠️ Overdue (1 day)" else "⚠️ Overdue ($overdueDays days)"
            Triple(label, PriorityHighBg, PriorityHighText)
        }
        isToday -> {
            val dueTime = deadline.dueTime
            if (dueTime != null) {
                val nowTime = LocalTime.now()
                if (nowTime.isBefore(dueTime)) {
                    val minutesUntil = ChronoUnit.MINUTES.between(nowTime, dueTime)
                    val hours = minutesUntil / 60
                    val mins = minutesUntil % 60
                    val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                    Triple("⏰ Due in $timeStr", AlertBannerBg, AlertBannerText)
                } else {
                    Triple("⚠️ Overdue today", PriorityHighBg, PriorityHighText)
                }
            } else {
                Triple("⏰ Due Today", AlertBannerBg, AlertBannerText)
            }
        }
        daysDiff == 1L -> Triple("Due Tomorrow", PrimaryBlueLight, PrimaryBlue)
        daysDiff in 2..7 -> Triple("Due in $daysDiff days", PrimaryBlueLight, PrimaryBlue)
        else -> Triple("In Progress", PrimaryBlueLight, PrimaryBlue)
    }

    val (priorityBg, priorityTint) = when (deadline.priority) {
        Priority.HIGH -> PriorityHighBg to PriorityHighText
        Priority.MEDIUM -> PriorityMediumBg to PriorityMediumText
        Priority.LOW -> PriorityLowBg to PriorityLowText
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (deadline.isCompleted) 0.55f else 1f,
        label = "hero_alpha"
    )

    val checkboxBorderColor = remember(deadline.isCompleted, deadline.priority) {
        if (deadline.isCompleted) PrimaryBlue
        else when (deadline.priority) {
            Priority.HIGH -> PriorityHighText
            Priority.MEDIUM -> PriorityMediumText
            Priority.LOW -> PriorityLowText
        }
    }

    val scope = rememberCoroutineScope()
    val wipeProgress = remember { Animatable(0f) }

    val handleToggleCompleted = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!deadline.isCompleted) {
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

    val heroShape = remember { RoundedCornerShape(24.dp) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(heroShape)
            .drawWithContent {
                drawContent()
                val progress = wipeProgress.value
                if (progress > 0f && progress <= 1f) {
                    val wipeWidth = size.width * progress
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
                    drawRect(
                        color = PrimaryBlue.copy(alpha = 0.85f),
                        topLeft = Offset(wipeWidth - 3.dp.toPx().coerceAtLeast(0f), 0f),
                        size = Size(3.dp.toPx(), size.height)
                    )
                }
            },
        shape = heroShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Category, Status, & Priority Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deadline.category.iconBackground,
                    border = BorderStroke(0.5.dp, deadline.category.iconTint.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = deadline.category.icon,
                            contentDescription = null,
                            tint = deadline.category.iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = deadline.category.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = deadline.category.iconTint
                        )
                    }
                }

                // Status & Priority on the right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Priority Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityBg,
                        border = BorderStroke(0.5.dp, priorityTint.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(priorityTint)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = deadline.priority.displayName(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityTint
                            )
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(18.dp))

                // Title with Direct Interactive Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Tactile Circular Completion Checkbox
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (deadline.isCompleted) PrimaryBlue else Color.Transparent)
                            .border(
                                width = if (deadline.isCompleted) 0.dp else 2.dp,
                                color = checkboxBorderColor,
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { handleToggleCompleted() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (deadline.isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title Text
                    Text(
                        text = deadline.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textDecoration = if (deadline.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(contentAlpha),
                        lineHeight = 28.sp
                    )
                }

                // Description with Editorial Left-Accent Bar
                if (!deadline.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(contentAlpha)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(deadline.category.iconTint.copy(alpha = 0.5f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = deadline.description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Quick Postpone / Reschedule Action Chips (when pending)
                if (!deadline.isCompleted && onReschedule != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "POSTPONE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QuickRescheduleChip(label = "+1 Day") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReschedule(deadline.dueDate.plusDays(1))
                            }
                            QuickRescheduleChip(label = "+3 Days") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReschedule(deadline.dueDate.plusDays(3))
                            }
                            QuickRescheduleChip(label = "+1 Week") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReschedule(deadline.dueDate.plusWeeks(1))
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRescheduleChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = PrimaryBlueLight,
        border = BorderStroke(0.5.dp, PrimaryBlue.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineHeroCardPreview() {
    DeadlineHeroCard(
        deadline = Deadline(
            id = 1L,
            title = "Machine Learning Term Paper",
            description = "Complete research synthesis, methodology section, and submit PDF.",
            dueDate = LocalDate.now().plusDays(1),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            isCompleted = false
        )
    )
}
