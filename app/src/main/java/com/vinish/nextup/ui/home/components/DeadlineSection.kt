package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.components.SectionHeader

/**
 * Reusable section component that displays a header and up to [maxItems] deadline cards,
 * with expandable view to see all items in the section.
 */
@Composable
fun DeadlineSection(
    title: String,
    deadlines: List<Deadline>,
    modifier: Modifier = Modifier,
    maxItems: Int = 2,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {},
    onDeadlineClick: ((Deadline) -> Unit)? = null
) {
    if (deadlines.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val canExpand = deadlines.size > maxItems
    val displayDeadlines = if (isExpanded) deadlines else deadlines.take(maxItems)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader(
            title = title,
            showViewAll = showSeeAll && canExpand,
            actionText = if (isExpanded) "Show less" else "See all",
            onViewAllClick = {
                isExpanded = !isExpanded
                onSeeAllClick()
            }
        )

        displayDeadlines.forEach { deadline ->
            DeadlineCard(
                deadline = deadline,
                onClick = onDeadlineClick?.let { { it(deadline) } }
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
