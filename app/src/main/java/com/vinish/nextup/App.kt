package com.vinish.nextup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vinish.nextup.navigation.AppNavigation
import com.vinish.nextup.navigation.Screen

@Composable
fun App(
    modifier: Modifier = Modifier,
    initialDeadlineId: Long? = null
) {
    val navController = rememberNavController()

    LaunchedEffect(initialDeadlineId) {
        if (initialDeadlineId != null && initialDeadlineId > 0) {
            navController.navigate(Screen.Details.createRoute(initialDeadlineId))
        }
    }

    AppNavigation(
        navController = navController,
        modifier = modifier
    )
}

