package com.vinish.nextup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextSecondary

@Composable
fun NextUpBottomNavigation(
    modifier: Modifier = Modifier,
    currentRoute: String? = "home",
    onItemClick: (String) -> Unit = {}
) {
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val navItemStyle = NavigationBarItemDefaults.colors(
        selectedIconColor = primaryColor,
        selectedTextColor = primaryColor,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = SurfaceWhite
    )
    NavigationBar(
        modifier = modifier,
        containerColor = SurfaceWhite,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onItemClick("home") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 12.sp,
                    fontWeight = if (currentRoute == "home") FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemStyle
        )

        NavigationBarItem(
            selected = currentRoute == "all",
            onClick = { onItemClick("all") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "all") Icons.AutoMirrored.Filled.FormatListBulleted else Icons.AutoMirrored.Outlined.FormatListBulleted,
                    contentDescription = "All"
                )
            },
            label = {
                Text(
                    text = "All",
                    fontSize = 12.sp,
                    fontWeight = if (currentRoute == "all") FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemStyle
        )

        NavigationBarItem(
            selected = currentRoute == "categories",
            onClick = { onItemClick("categories") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "categories") Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Category"
                )
            },
            label = {
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = if (currentRoute == "categories") FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemStyle
        )


        NavigationBarItem(
            selected = currentRoute == "calendar",
            onClick = { onItemClick("calendar") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "calendar") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendar"
                )
            },
            label = {
                Text(
                    text = "Calendar",
                    fontSize = 12.sp,
                    fontWeight = if (currentRoute == "calendar") FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemStyle
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onItemClick("profile") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text(
                    text = "Profile",
                    fontSize = 12.sp,
                    fontWeight = if (currentRoute == "profile") FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemStyle
        )
    }
}
