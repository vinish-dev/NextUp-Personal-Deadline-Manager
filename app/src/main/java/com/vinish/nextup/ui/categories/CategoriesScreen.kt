package com.vinish.nextup.ui.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.home.components.DeadlineCard
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.BorderStoke
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary

import androidx.compose.runtime.Immutable

@Immutable
private data class CategorySummary(
    val category: Category,
    val deadlines: List<Deadline>,
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val progress: Float
)

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = SampleDeadlines.sampleDeadlines,
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleCompleted: ((Deadline) -> Unit)? = null,
    onDeleteDeadline: ((Deadline) -> Unit)? = null
) {
    var expandedCategory by remember { mutableStateOf<Category?>(null) }

    val categorySummaries = remember(deadlines) {
        val grouped = deadlines.groupBy { it.category }
        Category.entries.map { category ->
            val list = grouped[category] ?: emptyList()
            val total = list.size
            val completed = list.count { it.isCompleted }
            CategorySummary(
                category = category,
                deadlines = list,
                totalCount = total,
                completedCount = completed,
                pendingCount = total - completed,
                progress = if (total > 0) completed.toFloat() / total else 0f
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // Editorial Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Text(
                    text = "ORGANIZATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Track deadlines grouped by project and life area",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Category Cards
        categorySummaries.forEach { summary ->
            val category = summary.category
            val categoryDeadlines = summary.deadlines
            val totalCount = summary.totalCount
            val completedCount = summary.completedCount
            val pendingCount = summary.pendingCount
            val progress = summary.progress
            val isExpanded = expandedCategory == category

            item(key = category.name) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(width = 1.dp, color = BorderLight)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedCategory = if (isExpanded) null else category
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon Container
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(category.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.displayName,
                                    tint = category.iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Name & Task count
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when {
                                        totalCount == 0 -> "No deadlines"
                                        pendingCount == 0 -> "All $totalCount completed"
                                        else -> "$pendingCount active · $completedCount done"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }

                            // Expand Arrow
                            if (totalCount > 0) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Progress bar if tasks exist
                        if (totalCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = category.iconTint,
                                trackColor = BorderLight,
                                strokeCap = StrokeCap.Round
                            )
                        }

                        // Expanded task list inside this category
                        AnimatedVisibility(
                            visible = isExpanded && totalCount > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoryDeadlines.forEach { deadline ->
                                    DeadlineCard(
                                        deadline = deadline,
                                        onClick = onDeadlineClick?.let { { it(deadline) } },
                                        onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                                        onDelete = onDeleteDeadline?.let { { it(deadline) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}