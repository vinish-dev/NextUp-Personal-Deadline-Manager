package com.vinish.nextup.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vinish.nextup.ui.home.components.GreetingSection

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item{
            GreetingSection(
                name = "Vinish"
            )
        }
    }
}