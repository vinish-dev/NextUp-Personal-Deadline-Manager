package com.vinish.nextup.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vinish.nextup.navigation.Screen
import com.vinish.nextup.ui.DeadlineViewModel
import com.vinish.nextup.ui.calendar.CalendarScreen
import com.vinish.nextup.ui.categories.CategoriesScreen
import com.vinish.nextup.ui.components.NextUpBottomNavigation
import com.vinish.nextup.ui.home.HomeScreen
import com.vinish.nextup.ui.home.model.OverviewType
import com.vinish.nextup.ui.profile.ProfileScreen
import kotlinx.coroutines.launch

/**
 * Main pager screen supporting horizontal swipe between the 4 main tabs:
 * Home (0) → All (1) → Calendar (2) → Profile (3)
 *
 * The '+' action is excluded from swipe as it represents an action, not a tab destination.
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

    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val coroutineScope = rememberCoroutineScope()

    // Back gesture returns to Home (page 0) when on secondary tabs
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    // Map pager index to corresponding bottom navigation route
    val currentRoute = when (pagerState.currentPage) {
        0 -> Screen.Home.route
        1 -> Screen.Categories.route
        2 -> Screen.Calendar.route
        3 -> Screen.Profile.route
        else -> Screen.Home.route
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NextUpBottomNavigation(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    when (route) {
                        Screen.Home.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                        Screen.Categories.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                        Screen.Calendar.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                        Screen.Profile.route -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                        "add" -> {
                            navController.navigate(Screen.Add.createRoute())
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
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
                    onAddDeadlineClick = {
                        navController.navigate(Screen.Add.createRoute())
                    },
                    onAvatarClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    },
                    onOverviewClick = { type ->
                        if (type == OverviewType.ALL_TASKS) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    }
                )

                // Page 1: Categories (All)
                1 -> CategoriesScreen(
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
                    }
                ) 

                // Page 2: Calendar
                2 -> CalendarScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    onDeadlineClick = { deadline ->
                        navController.navigate(Screen.Details.createRoute(deadline.id))
                    },
                    onAddDeadlineClick = { selectedDate ->
                        navController.navigate(Screen.Add.createRoute(selectedDate))
                    }
                )

                // Page 3: Profile
                3 -> ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    deadlines = deadlines,
                    showCompletedDeadlines = showCompletedInCategories,
                    onShowCompletedDeadlinesChange = { enabled ->
                        viewModel.setShowCompletedInCategories(enabled)
                    },
                    useSampleData = useSampleData,
                    onUseSampleDataChange = { enabled ->
                        viewModel.setUseSampleData(enabled)
                    }
                )
            }
        }
    }
}
