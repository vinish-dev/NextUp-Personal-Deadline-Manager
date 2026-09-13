package com.vinish.nextup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vinish.nextup.navigation.AppNavigation
import com.vinish.nextup.navigation.Screen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.vinish.nextup.ui.DeadlineViewModel

@Composable
fun App(
    modifier: Modifier = Modifier,
    initialDeadlineId: Long? = null,
    viewModel: DeadlineViewModel = viewModel()
) {
    val navController = rememberNavController()

    LaunchedEffect(initialDeadlineId) {
        if (initialDeadlineId != null && initialDeadlineId > 0) {
            navController.navigate(Screen.Details.createRoute(initialDeadlineId))
        }
    }

    AppNavigation(
        navController = navController,
        modifier = modifier,
        viewModel = viewModel
    )
}

