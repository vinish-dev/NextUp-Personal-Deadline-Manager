package com.vinish.nextup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.ui.add.AddDeadlineScreen
import com.vinish.nextup.ui.calendar.CalendarScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.categories.CategoryDetailScreen
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
            CategoriesScreen(
                modifier = modifier,
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryDetail.createRoute(category.name))
                }
            )
        }

        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: Category.OTHER.name
            val selectedCategory = try {
                Category.valueOf(categoryName)
            } catch (_: IllegalArgumentException) {
                Category.OTHER
            }

            CategoryDetailScreen(
                modifier = modifier,
                category = selectedCategory,
                deadlines = SampleDeadlines.sampleDeadlines.filter { it.category == selectedCategory },
                onBackClick = { navController.popBackStack() },
                onDeadlineClick = null
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(modifier = modifier)
        }
    }
}