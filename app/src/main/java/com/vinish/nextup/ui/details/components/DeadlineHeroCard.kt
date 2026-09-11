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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.vinish.nextup.ui.add.components.displayName
import com.vinish.nextup.ui.home.components.PriorityTag
import com.vinish.nextup.ui.theme.AlertBannerBg
import com.vinish.nextup.ui.theme.AlertBannerText
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.model.isOverdue
import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DeadlineHeroCard(
    deadline: Deadline,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val nowTime = LocalTime.now()
    val isOverdue = deadline.isOverdue(today, nowTime)
    val isToday = deadline.dueDate.isEqual(today) && !deadline.isCompleted && !isOverdue

    val (statusText, statusBg, statusColor) = when {
        deadline.isCompleted -> Triple("Completed", PriorityLowBg, PriorityLowText)
        isOverdue -> Triple("Overdue", PriorityHighBg, PriorityHighText)
        isToday -> Triple("Due Today", AlertBannerBg, AlertBannerText)
        else -> Triple("In Progress", PrimaryBlueLight, PrimaryBlue)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = BorderStoke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Category Icon Badge & Category Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(deadline.category.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deadline.category.icon,
                        contentDescription = deadline.category.name,
                        tint = deadline.category.iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = deadline.category.displayName(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = deadline.category.iconTint
                    )
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Information hierarchy: Title -> Description -> Status / Priority
            // Title
            Text(
                text = deadline.title.toTitleCase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Description
            if (!deadline.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = deadline.description.toSentenceCase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Status / Priority row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // Priority Badge
                PriorityTag(priority = deadline.priority)
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
            title = "DBMS Assignment",
            description = "Write and submit the final report.",
            dueDate = LocalDate.now(),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            isCompleted = false
        )
    )
}
