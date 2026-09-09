package com.vinish.nextup.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vinish.nextup.ui.theme.CategoryDocuments
import com.vinish.nextup.ui.theme.CategoryEducation
import com.vinish.nextup.ui.theme.CategoryFinance
import com.vinish.nextup.ui.theme.CategoryOther
import com.vinish.nextup.ui.theme.CategoryPersonal

private fun defaultCategoryIcon(name: String): ImageVector = when (name.uppercase()) {
    "EDUCATION" -> Icons.Filled.School
    "WORK" -> Icons.Filled.Code
    "FINANCE" -> Icons.Filled.CreditCard
    "PERSONAL" -> Icons.Filled.Person
    "DOCUMENTS" -> Icons.Filled.Description
    "OTHER" -> Icons.Filled.Task
    else -> Icons.AutoMirrored.Filled.Label
}

private fun defaultCategoryBackground(name: String): Color = when (name.uppercase()) {
    "EDUCATION" -> Color(0xFFEFF6FF)
    "WORK" -> Color(0xFFF3E8FF)
    "FINANCE" -> Color(0xFFECFDF5)
    "PERSONAL" -> Color(0xFFFFF7ED)
    "DOCUMENTS" -> Color(0xFFFEF3C7)
    "OTHER" -> Color(0xFFF1F5F9)
    else -> Color(0xFFE2E8F0)
}

private fun defaultCategoryTint(name: String): Color = when (name.uppercase()) {
    "EDUCATION" -> CategoryEducation
    "WORK" -> Color(0xFFA855F7)
    "FINANCE" -> CategoryFinance
    "PERSONAL" -> CategoryPersonal
    "DOCUMENTS" -> CategoryDocuments
    "OTHER" -> CategoryOther
    else -> CategoryOther
}

data class Category(
    val name: String,
    val icon: ImageVector = defaultCategoryIcon(name),
    val iconBackground: Color = defaultCategoryBackground(name),
    val iconTint: Color = defaultCategoryTint(name),
    val isCustom: Boolean = false
) {
    companion object {
        val EDUCATION = Category(
            name = "EDUCATION",
            icon = Icons.Filled.School,
            iconBackground = Color(0xFFEFF6FF),
            iconTint = CategoryEducation
        )
        val WORK = Category(
            name = "WORK",
            icon = Icons.Filled.Work,
            iconBackground = Color(0xFFF3E8FF),
            iconTint = Color(0xFFA855F7)
        )
        val FINANCE = Category(
            name = "FINANCE",
            icon = Icons.Filled.CreditCard,
            iconBackground = Color(0xFFECFDF5),
            iconTint = CategoryFinance
        )
        val PERSONAL = Category(
            name = "PERSONAL",
            icon = Icons.Filled.Person,
            iconBackground = Color(0xFFFFF7ED),
            iconTint = CategoryPersonal
        )
        val DOCUMENTS = Category(
            name = "DOCUMENTS",
            icon = Icons.Filled.Description,
            iconBackground = Color(0xFFFEF3C7),
            iconTint = CategoryDocuments
        )
        val OTHER = Category(
            name = "OTHER",
            icon = Icons.Filled.Task,
            iconBackground = Color(0xFFF1F5F9),
            iconTint = CategoryOther
        )

        val builtInCategories: List<Category> = listOf(EDUCATION, WORK, FINANCE, PERSONAL, DOCUMENTS, OTHER)
        private val builtInMap = builtInCategories.associateBy { it.name.uppercase() }

        fun fromName(rawName: String?): Category {
            val normalized = rawName?.trim()?.takeIf { it.isNotBlank() } ?: OTHER.name
            return builtInMap[normalized.uppercase()] ?: Category(
                name = normalized,
                icon = defaultCategoryIcon(normalized),
                iconBackground = defaultCategoryBackground(normalized),
                iconTint = defaultCategoryTint(normalized),
                isCustom = true
            )
        }

        fun allFrom(deadlines: List<Deadline>, customCategories: List<Category> = emptyList()): List<Category> {
            val seen = linkedSetOf<String>()
            val ordered = mutableListOf<Category>()

            builtInCategories.forEach { category ->
                seen.add(category.name.uppercase())
                ordered.add(category)
            }

            customCategories.forEach { category ->
                val normalized = category.name.trim()
                if (normalized.isNotBlank() && !seen.contains(normalized.uppercase())) {
                    seen.add(normalized.uppercase())
                    ordered.add(category)
                }
            }

            deadlines.map { it.category }.forEach { category ->
                val normalized = category.name.trim()
                if (normalized.isNotBlank() && !seen.contains(normalized.uppercase())) {
                    seen.add(normalized.uppercase())
                    ordered.add(category)
                }
            }

            return ordered
        }
    }

    fun displayName(): String = when (name.uppercase()) {
        "EDUCATION" -> "Education"
        "WORK" -> "Work"
        "FINANCE" -> "Finance"
        "PERSONAL" -> "Personal"
        "DOCUMENTS" -> "Documents"
        "OTHER" -> "Other"
        else -> name
    }

    fun matches(other: Category): Boolean = name.equals(other.name, ignoreCase = true)

    val backgroundColor: Color get() = iconBackground
    val iconBg: Color get() = iconBackground
}
