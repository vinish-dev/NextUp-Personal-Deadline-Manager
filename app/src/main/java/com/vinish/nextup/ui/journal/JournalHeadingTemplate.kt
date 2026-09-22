package com.vinish.nextup.ui.journal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Update
import androidx.compose.ui.graphics.vector.ImageVector

enum class JournalHeadingTemplate(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    TODAY(
        title = "Today",
        description = "Overview of the day",
        icon = Icons.Outlined.Today
    ),
    WHAT_HAPPENED(
        title = "What happened",
        description = "Key events and happenings",
        icon = Icons.AutoMirrored.Outlined.EventNote
    ),
    HIGHLIGHTS(
        title = "Highlights",
        description = "Best moments and wins",
        icon = Icons.Outlined.StarOutline
    ),
    THOUGHTS(
        title = "Thoughts",
        description = "Ideas and reflections",
        icon = Icons.Outlined.Lightbulb
    ),
    WHAT_I_LEARNED(
        title = "What I learned",
        description = "New insights and takeaways",
        icon = Icons.Outlined.School
    ),
    WHAT_WENT_WELL(
        title = "What went well",
        description = "Positives and achievements",
        icon = Icons.Outlined.ThumbUp
    ),
    CHALLENGES(
        title = "Challenges",
        description = "Obstacles and solutions",
        icon = Icons.Outlined.FitnessCenter
    ),
    GRATEFUL_FOR(
        title = "Things I'm grateful for",
        description = "Gratitude and appreciation",
        icon = Icons.Outlined.FavoriteBorder
    ),
    TOMORROW(
        title = "Tomorrow",
        description = "Plans and goals for tomorrow",
        icon = Icons.Outlined.Update
    ),
    NOTES(
        title = "Notes",
        description = "Quick notes and thoughts",
        icon = Icons.Outlined.EditNote
    );

    fun formatHeading(): String = "## $title\n\n"
}
