package com.vinish.nextup.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vinish.nextup.ui.theme.FabGreen
import com.vinish.nextup.ui.theme.PrimaryGreen

@Composable
fun AddDeadlineFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGreenTheme = MaterialTheme.colorScheme.primary == PrimaryGreen
    val containerColor = if (isGreenTheme) FabGreen else MaterialTheme.colorScheme.primary

    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = containerColor,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp,
            focusedElevation = 4.dp
        ),
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Deadline",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
