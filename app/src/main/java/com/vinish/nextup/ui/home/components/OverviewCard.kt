package com.vinish.nextup.ui.home.components
import com.vinish.nextup.ui.home.model.OverviewType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun OverviewCard(
    type: OverviewType,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = type.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                tint = type.iconTint,
                imageVector = type.icon,
                contentDescription = type.title
            )
            Spacer(Modifier.height(3.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))

            Text(
                text = type.title,
                color = type.titleColor.copy(0.7f),
                fontWeight = FontWeight.W500
            )
        }
    }
}