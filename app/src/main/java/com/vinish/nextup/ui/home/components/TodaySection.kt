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

@Composable
fun TodaySection(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = SampleDeadlines.sampleDeadlines,
    onSeeAllClick: () -> Unit = {},
    onDeadlineClick: ((Deadline) -> Unit)? = null
) {
    val displayDeadlines = deadlines.take(2)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader(
            title = "Today",
            showViewAll = true,
            onViewAllClick = onSeeAllClick
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
private fun TodaySectionPreview() {
    TodaySection(
        deadlines = SampleDeadlines.sampleDeadlines
    )
}
