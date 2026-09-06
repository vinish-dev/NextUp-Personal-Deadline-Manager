package com.vinish.nextup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vinish.nextup.ui.add.AddDeadlineScreen
import com.vinish.nextup.ui.calendar.CalendarScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.home.HomeScreen
import com.vinish.nextup.ui.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(modifier = modifier)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(modifier = modifier)
        }

        composable(Screen.Add.route) {
            AddDeadlineScreen(modifier = modifier)
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(modifier = modifier)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(modifier = modifier)
        }
    }
}