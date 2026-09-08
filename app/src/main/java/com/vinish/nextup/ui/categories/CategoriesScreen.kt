package com.vinish.nextup.ui.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.ui.add.components.displayName
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary

@Composable
fun CategoriesScreen(modifier: Modifier = Modifier) {
    val categoriesWithCount = Category.entries.map { category ->
        category to SampleDeadlines.sampleDeadlines.count { it.category == category }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(categoriesWithCount) { (category, deadlineCount) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(width = 1.dp, color = BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(category.iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.displayName(),
                            tint = category.iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.displayName(),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Text(
                            text = "$deadlineCount ${if (deadlineCount == 1) "deadline" else "deadlines"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deadlineCount.toString(),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenPreview() {
    CategoriesScreen()
}