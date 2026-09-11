package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.ui.home.model.OverviewItem
import com.vinish.nextup.ui.home.model.OverviewType

/**
 * Compact overview section displaying all four deadline states simultaneously
 * in a single horizontal row across the full screen width.
 */
@Composable
fun OverviewSection(
    modifier: Modifier = Modifier,
    overviewItems: List<OverviewItem>,
    onItemClick: ((OverviewType) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        overviewItems.forEach { item ->
            OverviewCard(
                type = item.type,
                count = item.count,
                modifier = Modifier.weight(1f),
                onClick = onItemClick?.let { { it(item.type) } }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OverviewSectionPreview() {
    OverviewSection(
        overviewItems = listOf(
            OverviewItem(OverviewType.OVERDUE, 2),
            OverviewItem(OverviewType.TODAY, 4),
            OverviewItem(OverviewType.TOMORROW, 1),
            OverviewItem(OverviewType.THIS_WEEK, 5)
        )
    )
}