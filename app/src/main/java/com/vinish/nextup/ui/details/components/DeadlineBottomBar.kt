package com.vinish.nextup.ui.details.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PriorityLowBg
import com.vinish.nextup.ui.theme.PriorityLowText

@Composable
fun DeadlineBottomBar(
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val buttonBgColor by animateColorAsState(
        targetValue = if (isCompleted) PriorityLowBg else PrimaryBlue,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_bg"
    )

    val buttonTextColor by animateColorAsState(
        targetValue = if (isCompleted) PriorityLowText else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_text"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleCompleted()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBgColor,
                contentColor = buttonTextColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = buttonTextColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isCompleted) "Completed ✓ — Tap to Reopen" else "Mark as Completed",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = buttonTextColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeadlineBottomBarPreview() {
    DeadlineBottomBar(
        isCompleted = false,
        onToggleCompleted = {},
        onEditClick = {}
    )
}
