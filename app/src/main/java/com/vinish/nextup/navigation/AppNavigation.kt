package com.vinish.nextup.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.ui.DeadlineViewModel
import com.vinish.nextup.ui.add.AddDeadlineScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.details.DeadlineDetailsScreen
import com.vinish.nextup.ui.journal.DailyJournalScreen
import com.vinish.nextup.ui.main.MainPagerScreen
import java.time.LocalDate

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: DeadlineViewModel = viewModel()
) {
    val deadlines by viewModel.deadlines.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            MainPagerScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier,
                initialPage = 0
            )
        }

        composable(Screen.AllTasks.route) {
            MainPagerScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier,
                initialPage = 1
            )
        }

        composable(Screen.Pages.route) {
            MainPagerScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier,
                initialPage = 2
            )
        }

        composable(Screen.Categories.route) {
            val showCompletedInCategories by viewModel.showCompletedInCategories.collectAsStateWithLifecycle()
            CategoriesScreen(
                modifier = modifier.fillMaxSize(),
                deadlines = deadlines,
                showCompletedDeadlines = showCompletedInCategories,
                onBackClick = { navController.popBackStack() },
                onDeadlineClick = { deadline ->
                    navController.navigate(Screen.Details.createRoute(deadline.id))
                },
                onToggleComplete = { deadline ->
                    viewModel.toggleCompleted(deadline)
                },
                onAddDeadlineClick = {
                    navController.navigate(Screen.Add.createRoute())
                }
            )
        }

        composable(Screen.Calendar.route) {
            MainPagerScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier,
                initialPage = 3
            )
        }

        composable(Screen.Profile.route) {
            MainPagerScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier,
                initialPage = 4
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
                },
                onSaveDeadline = { newDeadline ->
                    viewModel.saveDeadline(newDeadline)
                }
            )
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
            val deadlineFlow = remember(deadlineId) { viewModel.getDeadline(deadlineId) }
            val currentDeadline by deadlineFlow.collectAsStateWithLifecycle(initialValue = null)
            val activeDeadline = currentDeadline
                ?: deadlines.find { it.id == deadlineId }

            DeadlineDetailsScreen(
                modifier = modifier,
                deadlineId = deadlineId,
                initialDeadline = activeDeadline,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = { deadline ->
                    navController.navigate(Screen.Edit.createRoute(deadline.id))
                },
                onDeleteClick = { deadline ->
                    viewModel.deleteDeadline(deadline.id) {
                        navController.popBackStack()
                    }
                },
                onToggleCompleted = { deadline ->
                    viewModel.toggleCompleted(deadline)
                },
                onToggleSubtask = { deadline, subtask ->
                    viewModel.toggleSubtask(deadline, subtask)
                },
                onAddSubtask = { deadline, title ->
                    viewModel.addSubtask(deadline, title)
                }
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("deadlineId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val deadlineId = backStackEntry.arguments?.getLong("deadlineId") ?: return@composable
            val deadlineFlow = remember(deadlineId) { viewModel.getDeadline(deadlineId) }
            val currentDeadline by deadlineFlow.collectAsStateWithLifecycle(initialValue = null)
            val deadlineToEdit = currentDeadline
                ?: deadlines.find { it.id == deadlineId }

            if (deadlineToEdit != null) {
                AddDeadlineScreen(
                    modifier = modifier,
                    existingDeadline = deadlineToEdit,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveDeadline = { updatedDeadline ->
                        viewModel.saveDeadline(updatedDeadline)
                    }
                )
            }
        }

        composable(
            route = Screen.DailyJournal.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val dateStr = backStackEntry.arguments?.getString("date") ?: return@composable
            val date = try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                LocalDate.now()
            }
            DailyJournalScreen(
                date = date,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                modifier = modifier
            )
        }
    }
}