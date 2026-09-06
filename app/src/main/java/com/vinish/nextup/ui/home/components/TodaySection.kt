package com.vinish.nextup.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vinish.nextup.ui.components.SectionHeader

@Composable
fun TodaySection(modifier: Modifier = Modifier) {
    Column() {
        SectionHeader(title = "Today")
    }
}

