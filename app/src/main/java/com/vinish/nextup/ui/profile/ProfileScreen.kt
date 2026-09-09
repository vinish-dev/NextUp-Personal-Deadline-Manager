package com.vinish.nextup.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.ui.profile.components.MotivationCard
import com.vinish.nextup.ui.profile.components.PendingPrioritiesCard
import com.vinish.nextup.ui.profile.components.PreferencesCard
import com.vinish.nextup.ui.profile.components.ProfileHeader
import com.vinish.nextup.ui.profile.components.ProfileOverviewCard
import com.vinish.nextup.ui.profile.components.UserProfileCard
import com.vinish.nextup.ui.theme.BackgroundLight
import java.time.LocalDate

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = emptyList(),
    userName: String = "Vinish",
    onMoreClick: () -> Unit = {},
    onUserCardClick: () -> Unit = {},
    onPriorityClick: ((Priority) -> Unit)? = null,
    onRemindersClick: () -> Unit = {}
) {
    val totalDeadlines = deadlines.size
    val completedDeadlines = remember(deadlines) { deadlines.count { it.isCompleted } }
    val pendingDeadlines = totalDeadlines - completedDeadlines

    val today = remember { LocalDate.now() }
    val urgentDeadlinesCount = remember(deadlines, today) {
        deadlines.count { !it.isCompleted && (it.priority == Priority.HIGH || it.dueDate.isEqual(today)) }
    }

    val pendingList = remember(deadlines) { deadlines.filter { !it.isCompleted } }
    val highPriorityCount = remember(pendingList) { pendingList.count { it.priority == Priority.HIGH } }
    val mediumPriorityCount = remember(pendingList) { pendingList.count { it.priority == Priority.MEDIUM } }
    val lowPriorityCount = remember(pendingList) { pendingList.count { it.priority == Priority.LOW } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            ProfileHeader(
                onMoreClick = onMoreClick
            )
        }

        // User Identity Card
        item {
            UserProfileCard(
                userName = userName,
                workspaceName = "Personal Workspace",
                tagline = "Making progress, one deadline at a time",
                onCardClick = onUserCardClick
            )
        }

        // Monthly Overview Card with Stats
        item {
            ProfileOverviewCard(
                totalDeadlines = totalDeadlines,
                completedDeadlines = completedDeadlines,
                pendingDeadlines = pendingDeadlines,
                urgentDeadlinesCount = urgentDeadlinesCount
            )
        }

        // Pending Priorities Card
        item {
            PendingPrioritiesCard(
                highPriorityCount = highPriorityCount,
                mediumPriorityCount = mediumPriorityCount,
                lowPriorityCount = lowPriorityCount,
                onPriorityClick = onPriorityClick
            )
        }

        // Preferences Card
        item {
            PreferencesCard(
                onRemindersClick = onRemindersClick
            )
        }

        // Bottom Motivation Card
        item {
            MotivationCard()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    val sampleDeadlines = listOf(
        Deadline(
            id = 1,
            title = "Final Year Project Submission",
            dueDate = LocalDate.now(),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            isCompleted = false
        ),
        Deadline(
            id = 2,
            title = "Submit DBMS assignment",
            dueDate = LocalDate.now(),
            category = Category.EDUCATION,
            priority = Priority.HIGH,
            isCompleted = false
        ),
        Deadline(
            id = 3,
            title = "Quarterly Tax Review",
            dueDate = LocalDate.now().plusDays(2),
            category = Category.FINANCE,
            priority = Priority.MEDIUM,
            isCompleted = false
        )
    )

    ProfileScreen(
        deadlines = sampleDeadlines,
        userName = "Vinish"
    )
}
