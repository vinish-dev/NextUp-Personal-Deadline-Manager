package com.vinish.nextup.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    deadlines: List<Deadline>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> PrimaryBlue
        isToday -> PrimaryBlueLight
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        !isCurrentMonth -> TextTertiary
        isToday -> PrimaryBlue
        else -> TextPrimary
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onDateClick(date) }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )

                if (deadlines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayDots = deadlines.take(3)
                        displayDots.forEach { deadline ->
                            val dotColor = if (isSelected) {
                                Color.White.copy(alpha = 0.9f)
                            } else {
                                deadline.category.iconTint
                            }
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CalendarDayCell(
            date = LocalDate.now(),
            isSelected = true,
            isToday = true,
            isCurrentMonth = true,
            deadlines = listOf(
                Deadline(title = "Task", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.HIGH)
            ),
            onDateClick = {}
        )
        CalendarDayCell(
            date = LocalDate.now(),
            isSelected = false,
            isToday = true,
            isCurrentMonth = true,
            deadlines = listOf(
                Deadline(title = "Task", dueDate = LocalDate.now(), category = Category.WORK, priority = Priority.HIGH)
            ),
            onDateClick = {}
        )
        CalendarDayCell(
            date = LocalDate.now().plusDays(1),
            isSelected = false,
            isToday = false,
            isCurrentMonth = true,
            deadlines = emptyList(),
            onDateClick = {}
        )
    }
}
