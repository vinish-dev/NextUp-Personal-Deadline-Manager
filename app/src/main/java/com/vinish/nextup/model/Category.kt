package com.vinish.nextup.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vinish.nextup.ui.theme.CategoryDocuments
import com.vinish.nextup.ui.theme.CategoryEducation
import com.vinish.nextup.ui.theme.CategoryFinance
import com.vinish.nextup.ui.theme.CategoryOther
import com.vinish.nextup.ui.theme.CategoryPersonal

enum class Category(
    val icon: ImageVector,
    val iconBackground: Color,
    val iconTint: Color
) {
    EDUCATION(
        icon = Icons.Filled.Work,
        iconBackground = Color(0xFFEFF6FF),
        iconTint = CategoryEducation
    ),
    WORK(
        icon = Icons.Filled.Code,
        iconBackground = Color(0xFFF3E8FF),
        iconTint = Color(0xFFA855F7)
    ),
    FINANCE(
        icon = Icons.Filled.CreditCard,
        iconBackground = Color(0xFFECFDF5),
        iconTint = CategoryFinance
    ),
    PERSONAL(
        icon = Icons.Filled.Person,
        iconBackground = Color(0xFFFFF7ED),
        iconTint = CategoryPersonal
    ),
    DOCUMENTS(
        icon = Icons.Filled.Description,
        iconBackground = Color(0xFFFEF3C7),
        iconTint = CategoryDocuments
    ),
    OTHER(
        icon = Icons.Filled.Task,
        iconBackground = Color(0xFFF1F5F9),
        iconTint = CategoryOther
    );

    val backgroundColor: Color get() = iconBackground
    val iconBg: Color get() = iconBackground
}
