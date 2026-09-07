package com.vinish.nextup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.add.AddDeadlineScreen
import java.time.LocalDate
import com.vinish.nextup.ui.calendar.CalendarScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.details.DeadlineDetailsScreen
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
            HomeScreen(
                modifier = modifier,
                onDeadlineClick = { deadline ->
                    navController.navigate(Screen.Details.createRoute(deadline.id))
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                modifier = modifier,
                onAddDeadlineClick = { selectedDate ->
                    navController.navigate(Screen.Add.createRoute(selectedDate))
                },
                onDeadlineClick = { deadline ->
                    navController.navigate(Screen.Details.createRoute(deadline.id))
                }
            )
        }

        composable(
            route = Screen.Add.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val dateStr = backStackEntry.arguments?.getString("date")
            val initialDate = dateStr?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            }
            AddDeadlineScreen(
                modifier = modifier,
                initialDate = initialDate,
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(modifier = modifier)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(modifier = modifier)
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("deadlineId") {
                    type = NavType.LongType
                    defaultValue = 1L
                }
            )
        ) { backStackEntry ->
            val deadlineId = backStackEntry.arguments?.getLong("deadlineId") ?: 1L
            DeadlineDetailsScreen(
                modifier = modifier,
                deadlineId = deadlineId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate(Screen.Add.route)
                }
            )
        }
    }
}