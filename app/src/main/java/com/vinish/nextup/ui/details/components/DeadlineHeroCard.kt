package com.vinish.nextup.ui.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.home.components.PriorityTag
import com.vinish.nextup.ui.theme.AlertBannerBg
import com.vinish.nextup.ui.theme.AlertBannerText
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderMedium
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun DeadlineHeroCard(
    deadline: Deadline,
    modifier: Modifier = Modifier,
    onToggleCompleted: (() -> Unit)? = null
) {
    val today = LocalDate.now()
    val isOverdue = deadline.dueDate.isBefore(today) && !deadline.isCompleted
    val isToday = deadline.dueDate.isEqual(today) && !deadline.isCompleted
    val daysDiff = ChronoUnit.DAYS.between(today, deadline.dueDate)

    val (statusText, statusBg, statusColor) = when {
        deadline.isCompleted -> Triple("Completed", PriorityLowBg, PriorityLowText)
        isOverdue -> {
            val overdueDays = ChronoUnit.DAYS.between(deadline.dueDate, today)
            val label = if (overdueDays <= 1) "Overdue (1 day)" else "Overdue ($overdueDays days)"
            Triple(label, PriorityHighBg, PriorityHighText)
        }
        isToday -> Triple("Due Today", AlertBannerBg, AlertBannerText)
        daysDiff == 1L -> Triple("Due Tomorrow", PrimaryBlueLight, PrimaryBlue)
        daysDiff in 2..7 -> Triple("Due in $daysDiff days", PrimaryBlueLight, PrimaryBlue)
        else -> Triple("In Progress", PrimaryBlueLight, PrimaryBlue)
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (deadline.isCompleted) 0.55f else 1f,
        label = "hero_alpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Status and Priority Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Priority Badge
                PriorityTag(priority = deadline.priority)
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
                            color = if (deadline.isCompleted) PrimaryBlue else BorderMedium,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onToggleCompleted?.invoke() },
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
                            .background(PrimaryBlue.copy(alpha = 0.4f))
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
        }
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
