package com.vinish.nextup

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vinish.nextup.navigation.AppNavigation
import com.vinish.nextup.navigation.Screen
import com.vinish.nextup.ui.components.NextUpBottomNavigation

@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabRoutes = remember {
        setOf(
            Screen.Home.route,
            Screen.Calendar.route,
            Screen.Categories.route,
            Screen.Profile.route
        )
    }
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NextUpBottomNavigation(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        if (route == "add") {
                            navController.navigate(Screen.Add.createRoute())
                        } else if (route == Screen.Home.route) {
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
        }
    ) { innerPadding ->
        AppNavigation(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
