package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader

/**
 * Reusable section component that displays a header and up to [maxItems] deadline cards.
 */
@Composable
fun DeadlineSection(
    title: String,
    deadlines: List<Deadline>,
    modifier: Modifier = Modifier,
    maxItems: Int = 10,
    showSeeAll: Boolean = false,
    onSeeAllClick: () -> Unit = {},
    onDeadlineClick: ((Deadline) -> Unit)? = null,
    onToggleCompleted: ((Deadline) -> Unit)? = null,
    onDeleteDeadline: ((Deadline) -> Unit)? = null
) {
    if (deadlines.isEmpty()) return

    val displayDeadlines = deadlines.take(maxItems)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = title,
            showViewAll = showSeeAll,
            onViewAllClick = onSeeAllClick
        )

        displayDeadlines.forEach { deadline ->
            DeadlineCard(
                deadline = deadline,
                onClick = onDeadlineClick?.let { { it(deadline) } },
                onToggleCompleted = onToggleCompleted?.let { { it(deadline) } },
                onDelete = onDeleteDeadline?.let { { it(deadline) } }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineSectionPreview() {
    DeadlineSection(
        title = "Today",
        deadlines = SampleDeadlines.todayDeadlines
    )
}
