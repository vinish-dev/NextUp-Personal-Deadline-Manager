package com.vinish.nextup.ui.all

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader
import com.vinish.nextup.ui.home.components.DeadlineCard
import com.vinish.nextup.ui.home.components.HomeEmptyState
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.FabGreen
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary

@Composable
fun AllTasksScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleComplete: ((Deadline) -> Unit)? = null,
    onAddDeadlineClick: (() -> Unit)? = null,
    onEditDeadline: ((Deadline) -> Unit)? = null,
    onManageCategoriesClick: (() -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val availableCategories = remember(deadlines) {
        Category.allFrom(deadlines)
    }

    val pendingDeadlines = remember(deadlines) {
        deadlines
            .filter { !it.isCompleted }
            .sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
    }

    val completedDeadlines = remember(deadlines) {
        deadlines
            .filter { it.isCompleted }
            .sortedWith(compareByDescending<Deadline> { it.dueDate }.thenByDescending { it.dueTime })
    }

    val filteredPending = remember(pendingDeadlines, selectedCategory) {
        if (selectedCategory == null) pendingDeadlines
        else pendingDeadlines.filter { it.category.matches(selectedCategory!!) }
    }

    val filteredCompleted = remember(completedDeadlines, selectedCategory) {
        if (selectedCategory == null) completedDeadlines
        else completedDeadlines.filter { it.category.matches(selectedCategory!!) }
    }

    val pendingCount = filteredPending.size
    val completedCount = filteredCompleted.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "All Tasks",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View and manage all your deadlines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }

            // Category Filter Chips
            if (deadlines.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        item {
                            CategoryFilterChip(
                                label = "All",
                                isSelected = selectedCategory == null,
                                count = pendingDeadlines.size,
                                onClick = { selectedCategory = null }
                            )
                        }

                        items(availableCategories, key = { it.name }) { category ->
                            val catPendingCount = remember(pendingDeadlines, category) {
                                pendingDeadlines.count { it.category.matches(category) }
                            }
                            CategoryFilterChip(
                                label = category.displayName(),
                                isSelected = selectedCategory?.matches(category) == true,
                                icon = category.icon,
                                iconTint = category.iconTint,
                                count = catPendingCount,
                                onClick = {
                                    selectedCategory = if (selectedCategory?.matches(category) == true) null else category
                                }
                            )
                        }

                        if (onManageCategoriesClick != null) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                        .clickable(onClick = onManageCategoriesClick)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Category,
                                        contentDescription = "Manage Categories",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Manage",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (deadlines.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HomeEmptyState(
                        title = "No Tasks Found",
                        subtitle = "Create your first deadline to get started tracking your work.",
                        onAddDeadlineClick = onAddDeadlineClick
                    )
                }
            } else if (filteredPending.isEmpty() && filteredCompleted.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No ${selectedCategory?.displayName() ?: ""} tasks found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap below to view all tasks or add a new deadline.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { selectedCategory = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear filter")
                        }
                    }
                }
            } else {
                // Pending Deadlines Section
                if (filteredPending.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = if (filteredCompleted.isNotEmpty()) "Pending ($pendingCount)" else "Tasks ($pendingCount)",
                            showViewAll = false
                        )
                    }

                    items(filteredPending, key = { it.id }) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            onClick = { onDeadlineClick?.invoke(deadline) },
                            onToggleComplete = { onToggleComplete?.invoke(deadline) },
                            onEdit = { onEditDeadline?.invoke(deadline) }
                        )
                    }
                }

                // Completed Deadlines Section
                if (filteredCompleted.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        SectionHeader(
                            title = "Completed ($completedCount)",
                            showViewAll = false
                        )
                    }

                    items(filteredCompleted, key = { it.id }) { deadline ->
                        DeadlineCard(
                            deadline = deadline,
                            onClick = { onDeadlineClick?.invoke(deadline) },
                            onToggleComplete = { onToggleComplete?.invoke(deadline) }
                        )
                    }
                }
            }
        }

        // Add Deadline FAB
        if (onAddDeadlineClick != null) {
            com.vinish.nextup.ui.components.AddDeadlineFab(
                onClick = onAddDeadlineClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = FabGreen,
    count: Int? = null
) {
    val backgroundColor = if (isSelected) FabGreen else SurfaceWhite
    val contentColor = if (isSelected) Color.White else TextPrimary
    val borderColor = if (isSelected) FabGreen else BorderLight

    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else iconTint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun AllTasksScreenPreview() {
    AllTasksScreen(
        deadlines = SampleDeadlines.sampleDeadlines
    )
}
