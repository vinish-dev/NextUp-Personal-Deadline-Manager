package com.vinish.nextup.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.data.local.JournalEntryEntity
import com.vinish.nextup.ui.components.SectionHeader
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.FabGreen
import com.vinish.nextup.ui.theme.PrimaryGreen
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PagesScreen(
    entries: List<JournalEntryEntity>,
    onOpenJournal: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val todayDateString = remember(today) { today.toString() }

    val todayEntry = remember(entries, todayDateString) {
        entries.find { it.date == todayDateString }
    }

    val pastEntries = remember(entries, todayDateString) {
        entries.filter { it.date != todayDateString }
            .sortedByDescending { it.date }
    }

    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Pages",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily notes, thoughts & journal reflections",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }

            // Today's Page Hero Card
            item {
                TodayPageCard(
                    today = today,
                    formattedDay = today.format(dayFormatter),
                    todayEntry = todayEntry,
                    onOpenJournal = { onOpenJournal(today) }
                )
            }

            // Past Pages List
            if (pastEntries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeader(
                        title = "Past Pages (${pastEntries.size})",
                        showViewAll = false
                    )
                }

                items(pastEntries, key = { it.date }) { entry ->
                    val entryDate = remember(entry.date) {
                        try {
                            LocalDate.parse(entry.date)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (entryDate != null) {
                        PastPageCard(
                            date = entryDate,
                            entry = entry,
                            formattedDate = entryDate.format(dateFormatter),
                            isYesterday = entryDate == today.minusDays(1),
                            onClick = { onOpenJournal(entryDate) }
                        )
                    }
                }
            } else if (todayEntry == null) {
                // Empty state when absolutely no entries exist
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    PagesEmptyState(
                        onStartJournal = { onOpenJournal(today) }
                    )
                }
            }
        }

        // Quick FAB to write/edit today's page
        FloatingActionButton(
            onClick = { onOpenJournal(today) },
            shape = CircleShape,
            containerColor = FabGreen,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            Icon(
                imageVector = if (todayEntry != null) Icons.Outlined.Edit else Icons.Filled.Add,
                contentDescription = if (todayEntry != null) "Edit today's page" else "Write today's page",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TodayPageCard(
    today: LocalDate,
    formattedDay: String,
    todayEntry: JournalEntryEntity?,
    onOpenJournal: () -> Unit
) {
    val previewText = remember(todayEntry?.content) {
        cleanMarkdownPreview(todayEntry?.content.orEmpty())
    }
    val hasContent = previewText.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenJournal),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasContent) SurfaceWhite else Color(0xFFF0FDF4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasContent) 2.dp else 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasContent) BorderLight else Color(0xFFDCFCE7)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (hasContent) Color(0xFFEFF6FF) else Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasContent) Icons.Outlined.EditNote else Icons.AutoMirrored.Outlined.Article,
                            contentDescription = null,
                            tint = if (hasContent) Color(0xFF3B82F6) else PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Today's Page",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = formattedDay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                }

                // Status chip / action arrow
                if (hasContent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Written",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryGreen)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "+ Write",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasContent) {
                Text(
                    text = previewText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    text = "Reflect on your day, record learnings, or jot down ideas...",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun PastPageCard(
    date: LocalDate,
    entry: JournalEntryEntity,
    formattedDate: String,
    isYesterday: Boolean,
    onClick: () -> Unit
) {
    val previewText = remember(entry.content) {
        cleanMarkdownPreview(entry.content)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isYesterday) "Yesterday" else formattedDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = "Open page",
                    tint = TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
            }

            if (previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = previewText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun PagesEmptyState(
    onStartJournal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Pages Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Capture your daily reflections, notes, or achievements. Write your very first page today!",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        androidx.compose.material3.Button(
            onClick = onStartJournal,
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = FabGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Write Today's Page",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun cleanMarkdownPreview(content: String): String {
    return content.lines()
        .map { line -> line.trimStart().replace(Regex("^#{1,6}\\s*"), "").trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
}

