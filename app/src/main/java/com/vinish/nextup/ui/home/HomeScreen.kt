package com.vinish.nextup.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.home.components.DeadlineSection
import com.vinish.nextup.ui.home.components.GreetingSection
import com.vinish.nextup.ui.home.components.OverviewSection
import com.vinish.nextup.ui.home.model.OverviewItem
import com.vinish.nextup.ui.home.model.OverviewType

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDeadlineClick: ((Deadline) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        item {
            GreetingSection(
                name = "Vinish"
            )
        }

        item {
            OverviewSection(
                modifier = Modifier.padding(vertical = 8.dp),
                overviewItems = listOf(
                    OverviewItem(OverviewType.OVERDUE, 2),
                    OverviewItem(OverviewType.TODAY, 3),
                    OverviewItem(OverviewType.TOMORROW, 1),
                    OverviewItem(OverviewType.THIS_WEEK, 7)
                )
            )
        }

        item {
            DeadlineSection(
                title = "Today",
                deadlines = SampleDeadlines.todayDeadlines,
                onDeadlineClick = onDeadlineClick
            )
        }

        item {
            DeadlineSection(
                title = "Tomorrow",
                deadlines = SampleDeadlines.tomorrowDeadlines,
                showSeeAll = false,
                onDeadlineClick = onDeadlineClick
            )
        }

        item {
            DeadlineSection(
                title = "This Week",
                deadlines = SampleDeadlines.thisWeekDeadlines,
                onDeadlineClick = onDeadlineClick
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}