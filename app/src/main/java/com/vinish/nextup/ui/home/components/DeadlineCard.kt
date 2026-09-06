package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.CategoryDocuments
import com.vinish.nextup.ui.theme.CategoryEducation
import com.vinish.nextup.ui.theme.CategoryFinance
import com.vinish.nextup.ui.theme.CategoryOther
import com.vinish.nextup.ui.theme.CategoryPersonal
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Returns icon, background color, and tint color for a given category.
 */
fun getCategoryVisuals(category: Category): Triple<ImageVector, Color, Color> {
    return when (category) {
        Category.EDUCATION -> Triple(Icons.Filled.Work, Color(0xFFEFF6FF), CategoryEducation)
        Category.WORK -> Triple(Icons.Filled.Code, Color(0xFFF3E8FF), Color(0xFFA855F7))
        Category.FINANCE -> Triple(Icons.Filled.CreditCard, Color(0xFFECFDF5), CategoryFinance)
        Category.PERSONAL -> Triple(Icons.Filled.Person, Color(0xFFFFF7ED), CategoryPersonal)
        Category.DOCUMENTS -> Triple(Icons.Filled.Description, Color(0xFFFEF3C7), CategoryDocuments)
        Category.OTHER -> Triple(Icons.Filled.Task, Color(0xFFF1F5F9), CategoryOther)
    }
}

/**
 * Formats deadline date and time into a readable string (e.g. "Due today · 11:59 PM")
 * and returns whether an alert color should be used.
 */
fun formatDueDate(dueDate: LocalDate, dueTime: LocalTime?): Pair<String, Boolean> {
    val today = LocalDate.now()
    val isOverdue = dueDate.isBefore(today)
    val isToday = dueDate.isEqual(today)
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

    return Pair(fullStr, isAlert)
}

@Composable
fun DeadlineCard(
    deadline: Deadline,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (iconVec, iconBg, iconTint) = getCategoryVisuals(deadline.category)
    val (subtitleText, isAlert) = formatDueDate(deadline.dueDate, deadline.dueTime)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderStoke)
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
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = deadline.category.name,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Due Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deadline.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAlert) PriorityHighText else TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Priority Badge
            PriorityTag(priority = deadline.priority)
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
