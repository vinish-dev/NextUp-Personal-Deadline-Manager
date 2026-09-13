package com.vinish.nextup.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vinish.nextup.navigation.Screen
import com.vinish.nextup.ui.DeadlineViewModel
import com.vinish.nextup.ui.all.AllTasksScreen
import com.vinish.nextup.ui.calendar.CalendarScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.components.NextUpBottomNavigation
import com.vinish.nextup.ui.home.HomeScreen
import com.vinish.nextup.ui.home.model.OverviewType
import com.vinish.nextup.ui.profile.ProfileScreen
import kotlinx.coroutines.launch

/**
 * Main pager screen supporting horizontal swipe between the 5 main tabs:
 * Home (0) → All (1) → Category (2) → Calendar (3) → Profile (4)
 *
 * The '+' action is a floating action button on screens, not a swipeable page.
 */
@Composable
fun MainPagerScreen(
    navController: NavHostController,
    viewModel: DeadlineViewModel,
    modifier: Modifier = Modifier,
    initialPage: Int = 0 // Defaults to Home
) {
    val deadlines by viewModel.deadlines.collectAsStateWithLifecycle()
    val showCompletedInCategories by viewModel.showCompletedInCategories.collectAsStateWithLifecycle()
    val useSampleData by viewModel.useSampleData.collectAsStateWithLifecycle()
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = initialPage) { 5 }
    val coroutineScope = rememberCoroutineScope()

    // Back gesture returns to Home (page 0) when on secondary tabs
    val isNotHome by remember {
        derivedStateOf { pagerState.currentPage != 0 }
    }
    BackHandler(enabled = isNotHome) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val currentRoute = when (pagerState.currentPage) {
                0 -> Screen.Home.route
                1 -> Screen.AllTasks.route
                2 -> Screen.Categories.route
                3 -> Screen.Calendar.route
                4 -> Screen.Profile.route
                else -> Screen.Home.route
            }
            NextUpBottomNavigation(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    when (route) {
                        Screen.Home.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                        Screen.AllTasks.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                        Screen.Categories.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                        Screen.Calendar.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                        Screen.Profile.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { page -> page },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                // Page 0: Home
                0 -> HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    onDeadlineClick = { deadline ->
                        navController.navigate(Screen.Details.createRoute(deadline.id))
                    },
                    onToggleComplete = { deadline ->
                        viewModel.toggleCompleted(deadline)
                    },
                    onAddDeadlineClick = {
                        navController.navigate(Screen.Add.createRoute())
                    },
                    onAvatarClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(4)
                        }
                    },
                    onOverviewClick = { type ->
                        if (type == OverviewType.ALL_TASKS) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    },
                    onEditDeadline = { deadline ->
                        navController.navigate(Screen.Edit.createRoute(deadline.id))
                    }
                )

                // Page 1: All Tasks
                1 -> AllTasksScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    onDeadlineClick = { deadline ->
                        navController.navigate(Screen.Details.createRoute(deadline.id))
                    },
                    onToggleComplete = { deadline ->
                        viewModel.toggleCompleted(deadline)
                    },
                    onAddDeadlineClick = {
                        navController.navigate(Screen.Add.createRoute())
                    },
                    onEditDeadline = { deadline ->
                        navController.navigate(Screen.Edit.createRoute(deadline.id))
                    }
                )

                // Page 2: Category
                2 -> CategoriesScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    showCompletedDeadlines = showCompletedInCategories,
                    onBackClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
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

                // Page 3: Calendar
                3 -> CalendarScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    onDeadlineClick = { deadline ->
                        navController.navigate(Screen.Details.createRoute(deadline.id))
                    },
                    onToggleComplete = { deadline ->
                        viewModel.toggleCompleted(deadline)
                    },
                    onAddDeadlineClick = { selectedDate ->
                        navController.navigate(Screen.Add.createRoute(selectedDate))
                    },
                    onEditDeadline = { deadline ->
                        navController.navigate(Screen.Edit.createRoute(deadline.id))
                    }
                )

                // Page 4: Profile
                4 -> ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    showCompletedDeadlines = showCompletedInCategories,
                    onShowCompletedDeadlinesChange = { enabled ->
                        viewModel.setShowCompletedInCategories(enabled)
                    },
                    useSampleData = useSampleData,
                    onUseSampleDataChange = { enabled ->
                        viewModel.setUseSampleData(enabled)
                    },
                    appTheme = appTheme,
                    onThemeChange = { theme ->
                        viewModel.setAppTheme(theme)
                    }
                )
            }
        }
    }
}
