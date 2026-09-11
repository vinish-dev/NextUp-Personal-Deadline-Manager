package com.vinish.nextup

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vinish.nextup.navigation.AppNavigation
import com.vinish.nextup.navigation.Screen
import com.vinish.nextup.ui.components.NextUpBottomNavigation

@Composable
fun App(
    modifier: Modifier = Modifier,
    initialDeadlineId: Long? = null
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(initialDeadlineId) {
        if (initialDeadlineId != null && initialDeadlineId > 0) {
            navController.navigate(Screen.Details.createRoute(initialDeadlineId))
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NextUpBottomNavigation(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    if (route == Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavigation(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
