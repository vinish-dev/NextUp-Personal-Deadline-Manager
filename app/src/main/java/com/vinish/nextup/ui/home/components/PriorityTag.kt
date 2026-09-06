package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.theme.PriorityHighBg
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.PriorityMediumBg
import com.vinish.nextup.ui.theme.PriorityMediumText

@Composable
fun PriorityTag(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (priority) {
        Priority.HIGH -> Triple(PriorityHighBg, PriorityHighText, "High")
        Priority.MEDIUM -> Triple(PriorityMediumBg, PriorityMediumText, "Medium")
        Priority.LOW -> Triple(PriorityLowBg, PriorityLowText, "Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
