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
        "add?date={date}&title={title}&desc={desc}&time={time}&category={category}&priority={priority}",
        "Add",
        Icons.Outlined.AddCircleOutline
    ) {
        fun createRoute(
            date: java.time.LocalDate? = null,
            title: String? = null,
            description: String? = null,
            time: java.time.LocalTime? = null,
            category: String? = null,
            priority: String? = null
        ): String {
            val params = mutableListOf<String>()
            if (date != null) params.add("date=$date")
            if (!title.isNullOrBlank()) params.add("title=${java.net.URLEncoder.encode(title, "UTF-8")}")
            if (!description.isNullOrBlank()) params.add("desc=${java.net.URLEncoder.encode(description, "UTF-8")}")
            if (time != null) params.add("time=$time")
            if (!category.isNullOrBlank()) params.add("category=$category")
            if (!priority.isNullOrBlank()) params.add("priority=$priority")
            return if (params.isEmpty()) "add" else "add?" + params.joinToString("&")
        }
    }

    data object Categories : Screen(
        "categories",
        "Categories",
        Icons.Outlined.Category
    )

    data object Profile : Screen(
        "profile",
        "Profile",
        Icons.Outlined.Person
    )

    data object Details : Screen(
        "details/{deadlineId}",
        "Details",
        Icons.Outlined.Home
    ) {
        fun createRoute(deadlineId: Long): String = "details/$deadlineId"
    }

    data object Edit : Screen(
        "edit/{deadlineId}",
        "Edit",
        Icons.Outlined.AddCircleOutline
    ) {
        fun createRoute(deadlineId: Long): String = "edit/$deadlineId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Add,
    Screen.Categories,
    Screen.Profile
)