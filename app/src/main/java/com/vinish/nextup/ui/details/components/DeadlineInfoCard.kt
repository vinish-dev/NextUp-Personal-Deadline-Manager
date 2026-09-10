package com.vinish.nextup.ui.details.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.add.components.displayName
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.PriorityMediumBg
import com.vinish.nextup.ui.theme.PriorityMediumText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.util.Locale

private val InfoDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val InfoTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

@Composable
fun DeadlineInfoCard(
    deadline: Deadline,
    modifier: Modifier = Modifier
) {
    val dateString = deadline.dueDate.format(InfoDateFormatter)
    val timeString = deadline.dueTime?.format(InfoTimeFormatter)
    val dueDateTimeString = if (timeString != null) "$dateString\n$timeString" else dateString

    val reminderString = deadline.reminder?.displayName() ?: "None"
    val repeatString = deadline.recurrence?.displayName() ?: "None"

    val (priorityBg, priorityTint) = when (deadline.priority) {
        Priority.HIGH -> PriorityHighBg to PriorityHighText
        Priority.MEDIUM -> PriorityMediumBg to PriorityMediumText
        Priority.LOW -> PriorityLowBg to PriorityLowText
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Due Date Cell
            MetadataCell(
                icon = Icons.Filled.CalendarMonth,
                iconTint = PrimaryBlue,
                iconBg = PrimaryBlueLight,
                label = "Due Date",
                value = dueDateTimeString,
                modifier = Modifier.weight(1f)
            )

            // Priority Cell
            MetadataCell(
                icon = Icons.Filled.Flag,
                iconTint = priorityTint,
                iconBg = priorityBg,
                label = "Priority",
                value = deadline.priority.displayName(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Reminder Cell
            MetadataCell(
                icon = Icons.Outlined.Notifications,
                iconTint = Color(0xFFEAB308),
                iconBg = Color(0xFFFEF9C3),
                label = "Reminder",
                value = reminderString,
                modifier = Modifier.weight(1f)
            )

            // Repeat Cell
            MetadataCell(
                icon = Icons.Filled.Repeat,
                iconTint = Color(0xFF8B5CF6),
                iconBg = Color(0xFFEDE9FE),
                label = "Recurrence",
                value = repeatString,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetadataCell(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineInfoCardPreview() {
    DeadlineInfoCard(
        deadline = com.vinish.nextup.data.sample.SampleDeadlines.sampleDeadlines.first()
    )
}
