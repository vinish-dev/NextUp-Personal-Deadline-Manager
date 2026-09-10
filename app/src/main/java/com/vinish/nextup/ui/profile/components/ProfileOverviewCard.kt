package com.vinish.nextup.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.SurfaceSubtle
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary

@Composable
fun ProfileOverviewCard(
    totalDeadlines: Int,
    completedDeadlines: Int,
    pendingDeadlines: Int,
    urgentDeadlinesCount: Int,
    modifier: Modifier = Modifier,
    selectedTimeframe: String = "This Month",
    onTimeframeClick: () -> Unit = {}
) {
    val completionRatio = if (totalDeadlines > 0) {
        (completedDeadlines.toFloat() / totalDeadlines.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val completionPercentage = (completionRatio * 100).toInt()

    val subtitleText = when {
        urgentDeadlinesCount > 0 -> "$urgentDeadlinesCount urgent deadline${if (urgentDeadlinesCount > 1) "s require" else " requires"} your attention today."
        pendingDeadlines > 0 -> "$pendingDeadlines pending deadline${if (pendingDeadlines > 1) "s" else ""} remaining."
        else -> "All caught up! No pending deadlines."
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderStoke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Overview bar chart icon
                /*Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
*/
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overview",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timeframe Pill Button
                /*Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceSubtle)
                        .clickable(onClick = onTimeframeClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = selectedTimeframe,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }*/
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats 3-column Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(
                    icon = Icons.Outlined.Description,
                    iconBg = Color(0xFFEBF3FE),
                    iconTint = PrimaryBlue,
                    count = totalDeadlines,
                    label = "Total Deadlines",
                    modifier = Modifier.weight(1f)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(BorderLight.copy(alpha = 0.6f))
                )

                StatColumn(
                    icon = Icons.Rounded.Check,
                    iconBg = Color(0xFFE8F8EE),
                    iconTint = Color(0xFF10B981),
                    count = completedDeadlines,
                    label = "Completed",
                    modifier = Modifier.weight(1f)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(BorderLight.copy(alpha = 0.6f))
                )

                StatColumn(
                    icon = Icons.Outlined.Schedule,
                    iconBg = Color(0xFFFFF4E5),
                    iconTint = Color(0xFFF59E0B),
                    count = pendingDeadlines,
                    label = "Pending",
                    modifier = Modifier.weight(1f)
                )
            }

//            Spacer(modifier = Modifier.height(20.dp))

            // Completion Rate Row//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Completion Rate",
//                    fontSize = 13.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = TextPrimary
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                LinearProgressIndicator(
//                    progress = { completionRatio },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(8.dp)
//                        .clip(RoundedCornerShape(4.dp)),
//                    color = PrimaryBlue,
//                    trackColor = Color(0xFFE2E8F0)
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Text(
//                    text = "$completionPercentage%",
//                    fontSize = 13.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = TextPrimary
//                )
//            }
        }
    }
}

@Composable
private fun StatColumn(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileOverviewCardPreview() {
    ProfileOverviewCard(
        totalDeadlines = 3,
        completedDeadlines = 0,
        pendingDeadlines = 3,
        urgentDeadlinesCount = 2
    )
}
