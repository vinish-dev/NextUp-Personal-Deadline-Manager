package com.vinish.nextup.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.categories.components.AddCategoryCard
import com.vinish.nextup.ui.categories.components.CategoryItemCard
import com.vinish.nextup.ui.categories.components.CategoryStatsCard
import com.vinish.nextup.ui.categories.components.CategoryTopBar
import com.vinish.nextup.ui.theme.BackgroundLight
import java.time.LocalDate

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onCategoryClick: ((Category) -> Unit)? = null,
    onAddCategoryClick: (() -> Unit)? = null
) {
    val totalDeadlines = deadlines.size
    val completedDeadlines = remember(deadlines) { deadlines.count { it.isCompleted } }
    val pendingDeadlines = totalDeadlines - completedDeadlines

    val displayCategories = remember {
        listOf(
            Category.EDUCATION,
            Category.PERSONAL,
            Category.WORK,
//            Category.HEALTH,
            Category.FINANCE,
            Category.DOCUMENTS,
            Category.OTHER
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        item {
            CategoryTopBar(
                onMoreClick = onMoreClick,
                onBackClick = onBackClick
            )
        }

        // Summary Stats Card at top
        // item {
        //     CategoryStatsCard(
        //         totalDeadlines = totalDeadlines,
        //         completedDeadlines = completedDeadlines,
        //         pendingDeadlines = pendingDeadlines
        //     )
        // }

        // Category Cards
        items(displayCategories, key = { it.name }) { category ->
            val categoryDeadlines = remember(deadlines, category) {
                deadlines.filter { it.category == category }
            }
            val total = categoryDeadlines.size
            val completed = remember(categoryDeadlines) {
                categoryDeadlines.count { it.isCompleted }
            }

            CategoryItemCard(
                category = category,
                totalCount = total,
                completedCount = completed,
                onClick = onCategoryClick?.let { { it(category) } }
            )
        }

        // Add Custom Category Card
        item {
            AddCategoryCard(
                onClick = onAddCategoryClick
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenPreview() {
    val sampleList = listOf(
        // Academic (5 total, 2 completed)
        Deadline(id = 1, title = "A1", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.HIGH, isCompleted = true),
        Deadline(id = 2, title = "A2", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.MEDIUM, isCompleted = true),
        Deadline(id = 3, title = "A3", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.LOW, isCompleted = false),
        Deadline(id = 4, title = "A4", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.MEDIUM, isCompleted = false),
        Deadline(id = 5, title = "A5", dueDate = LocalDate.now(), category = Category.EDUCATION, priority = Priority.HIGH, isCompleted = false),
        // Personal (2 total, 1 completed)
        Deadline(id = 6, title = "P1", dueDate = LocalDate.now(), category = Category.PERSONAL, priority = Priority.LOW, isCompleted = true),
        Deadline(id = 7, title = "P2", dueDate = LocalDate.now(), category = Category.PERSONAL, priority = Priority.MEDIUM, isCompleted = false),
        // Work (2 total, 0 completed)
        Deadline(id = 8, title = "W1", dueDate = LocalDate.now(), category = Category.WORK, priority = Priority.HIGH, isCompleted = false),
        Deadline(id = 9, title = "W2", dueDate = LocalDate.now(), category = Category.WORK, priority = Priority.MEDIUM, isCompleted = false),
        // Finance (1 total, 0 completed)
        Deadline(id = 11, title = "F1", dueDate = LocalDate.now(), category = Category.FINANCE, priority = Priority.MEDIUM, isCompleted = false),
        // Other (1 total, 0 completed)
        Deadline(id = 12, title = "O1", dueDate = LocalDate.now(), category = Category.OTHER, priority = Priority.LOW, isCompleted = false)
    )

    CategoriesScreen(
        deadlines = sampleList
    )
}