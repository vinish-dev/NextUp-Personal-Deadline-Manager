package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.vinish.nextup.model.toTitleCase
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
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Holds formatted date/time text and alert indicator for a deadline.
 */
data class DueDateInfo(
    val text: String,
    val isAlert: Boolean
) {
    val formattedText: String get() = text
}

/**
 * Formats deadline date and time into a [DueDateInfo] object (e.g. "Due today · 11:59 PM")
 * and determines whether an alert color should be used.
 */
fun formatDueDate(dueDate: LocalDate, dueTime: LocalTime?): DueDateInfo {
    val today = LocalDate.now()
    val nowTime = LocalTime.now()
    val isOverdue = dueDate.isBefore(today) || (dueDate.isEqual(today) && dueTime != null && dueTime.isBefore(nowTime))
    val isToday = dueDate.isEqual(today) && !isOverdue
    val isTomorrow = dueDate.isEqual(today.plusDays(1))

    val dateStr = when {
        isOverdue -> "Overdue"
        isToday -> "Due today"
        isTomorrow -> "Due tomorrow"
        else -> dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    }

    val timeStr = dueTime?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
    val fullStr = if (timeStr != null) "$dateStr · $timeStr" else dateStr
    val isAlert = isOverdue || isToday

    return DueDateInfo(
        text = fullStr,
        isAlert = isAlert
    )
}

@Composable
fun DeadlineCard(
    deadline: Deadline,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onToggleComplete: (() -> Unit)? = null
) {
    val dueDateInfo = formatDueDate(deadline.dueDate, deadline.dueTime)
    val isDone = deadline.isCompleted

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isDone) Modifier.alpha(0.72f) else Modifier)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) SurfaceSubtle.copy(alpha = 0.6f) else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDone) BorderLight else BorderStoke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDone) deadline.category.iconBackground.copy(alpha = 0.5f)
                        else deadline.category.iconBackground
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = deadline.category.icon,
                    contentDescription = deadline.category.name,
                    tint = if (isDone) deadline.category.iconTint.copy(alpha = 0.7f) else deadline.category.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Due Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deadline.title.toTitleCase(),
                    fontSize = 16.sp,
                    fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Bold,
                    color = if (isDone) TextTertiary else TextPrimary,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dueDateInfo.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDone) TextTertiary else if (dueDateInfo.isAlert) PriorityHighText else TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Priority Badge & Completion Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isDone) {
                    PriorityTag(priority = deadline.priority)
                    if (onToggleComplete != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, BorderMedium, CircleShape)
                                .clickable(onClick = onToggleComplete)
                        )
                    }
                } else {
                    PriorityTag(
                        priority = deadline.priority,
                        modifier = Modifier.alpha(0.6f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CheckboxChecked)
                            .then(
                                if (onToggleComplete != null) Modifier.clickable(onClick = onToggleComplete)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Mark incomplete",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineCardPreview() {
    DeadlineCard(
        deadline = Deadline(
            id = 1L,
            title = "DBMS Assignment",
            dueDate = LocalDate.now(),
            dueTime = LocalTime.of(23, 59),
            category = Category.EDUCATION,
            priority = Priority.HIGH
        )
    )
}
