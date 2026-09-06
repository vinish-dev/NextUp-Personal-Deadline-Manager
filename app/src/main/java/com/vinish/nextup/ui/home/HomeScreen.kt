package com.vinish.nextup.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vinish.nextup.ui.home.components.GreetingSection
import com.vinish.nextup.ui.home.components.OverviewSection
import com.vinish.nextup.ui.home.model.OverviewItem

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item{
            GreetingSection(
                name = "Vinish"
            )
        }

        item {
            OverviewSection(
                overviewItems = listOf(
                    OverviewItem(OverviewType.OVERDUE, 2),
                    OverviewItem(OverviewType.TODAY, 3),
                    OverviewItem(OverviewType.TOMORROW, 1),
                    OverviewItem(OverviewType.THIS_WEEK, 7)
                )
            )
        }
    }
}