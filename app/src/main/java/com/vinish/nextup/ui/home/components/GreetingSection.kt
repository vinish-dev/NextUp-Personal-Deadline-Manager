package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import java.util.Calendar

/**
 * Returns a time-appropriate greeting message (Good morning, Good afternoon, Good evening).
 */
fun getGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun GreetingSection(
    name: String,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
    greeting: String = getGreetingMessage(),
    subtitle: String = "Stay on top of what matters",
    userInitial: String = name.firstOrNull()?.uppercase() ?: "V",
    onAvatarClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, $name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PrimaryBlueLight)
                .then(
                    if (onAvatarClick != null) Modifier.clickable(onClick = onAvatarClick)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitial,
                color = PrimaryBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingSectionPreview() {
    GreetingSection(
        name = "Vinish"
    )
}
