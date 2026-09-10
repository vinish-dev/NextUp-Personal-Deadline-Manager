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
import androidx.compose.runtime.remember
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

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ripple
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalTime
import java.time.ZoneId

private val InfoDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
private val InfoTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

@Composable
fun DeadlineInfoCard(
    deadline: Deadline,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateString = deadline.dueDate.format(InfoDateFormatter)
    val timeString = deadline.dueTime?.format(InfoTimeFormatter)

    val reminderString = deadline.reminder?.displayName() ?: "None"
    val repeatString = deadline.recurrence?.displayName() ?: "None"

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
                .padding(20.dp)
        ) {
            // Section Title
            Text(
                text = "Schedule & Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row 1: Separate Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dedicated Date Cell
                MetadataCell(
                    icon = Icons.Filled.CalendarMonth,
                    iconTint = PrimaryBlue,
                    iconBg = PrimaryBlueLight,
                    label = "Date",
                    value = dateString,
                    modifier = Modifier.weight(1f)
                )

                // Dedicated Time Cell
                MetadataCell(
                    icon = Icons.Outlined.AccessTime,
                    iconTint = Color(0xFF0D9488),
                    iconBg = Color(0xFFCCFBF1),
                    label = "Time",
                    value = timeString ?: "All Day",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Reminder and Repeat
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
                    label = "Repeat",
                    value = repeatString,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Integrated Export to Device Calendar Action
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, deadline.title)
                            if (!deadline.description.isNullOrBlank()) {
                                putExtra(CalendarContract.Events.DESCRIPTION, deadline.description)
                            }
                            val time = deadline.dueTime ?: LocalTime.of(9, 0)
                            val startMillis = deadline.dueDate.atTime(time)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, deadline.dueTime == null)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                    },
                shape = RoundedCornerShape(14.dp),
                color = BackgroundLight,
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export to Calendar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Sync with Google Calendar or system schedule",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
        shape = RoundedCornerShape(14.dp),
        color = BackgroundLight,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
