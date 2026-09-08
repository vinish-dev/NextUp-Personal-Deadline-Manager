package com.vinish.nextup.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        "home",
        "Home",
        Icons.Outlined.Home
    )

    data object Calendar : Screen(
        "calendar",
        "Calendar",
        Icons.Outlined.CalendarMonth
    )

    data object Add : Screen(
        "add",
        "Add",
        Icons.Outlined.AddCircleOutline
    )

    data object Categories : Screen(
        "categories",
        "Categories",
        Icons.Outlined.Category
    )

    data object CategoryDetail : Screen(
        "category/{categoryName}",
        "Category",
        Icons.Outlined.Category
    ) {
        fun createRoute(categoryName: String): String = "category/$categoryName"
    }

    data object Profile : Screen(
        "profile",
        "Profile",
        Icons.Outlined.Person
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Add,
    Screen.Categories,
    Screen.Profile
)