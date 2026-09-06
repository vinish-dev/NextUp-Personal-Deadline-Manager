package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vinish.nextup.ui.home.model.OverviewItem


@Composable
fun OverviewSection(
    modifier: Modifier = Modifier,
    overviewItems: List<OverviewItem>
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(overviewItems) {
            OverviewCard(
                type = it.type,
                count = it.count
            )
        }
    }
}