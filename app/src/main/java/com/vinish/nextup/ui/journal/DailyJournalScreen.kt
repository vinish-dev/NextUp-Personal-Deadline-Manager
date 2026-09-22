package com.vinish.nextup.ui.journal

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinish.nextup.ui.DeadlineViewModel
import com.vinish.nextup.ui.theme.AlertBannerBg
import com.vinish.nextup.ui.theme.AlertBannerText
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyJournalScreen(
    date: LocalDate,
    viewModel: DeadlineViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val journalEntryFlow = remember(date) { viewModel.getJournalEntry(date) }
    val journalEntry by journalEntryFlow.collectAsStateWithLifecycle(initialValue = null)

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isInitialized by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showInsertBottomSheet by remember { mutableStateOf(false) }

    val insertSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Initialize content once loaded
    LaunchedEffect(journalEntry) {
        if (!isInitialized && journalEntry != null) {
            val initial = journalEntry?.content.orEmpty()
            textFieldValue = TextFieldValue(
                text = initial,
                selection = TextRange(initial.length)
            )
            isInitialized = true
        }
    }

    // Auto-save on exit
    val saveAndExit = {
        viewModel.saveJournalEntry(date, textFieldValue.text)
        onBackClick()
    }

    BackHandler {
        saveAndExit()
    }

    // Insert heading function at cursor position
    fun insertHeadingTemplate(headingTitle: String) {
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection

        val start = selection.start.coerceIn(0, currentText.length)
        val end = selection.end.coerceIn(0, currentText.length)

        val prefix = currentText.take(start)
        val suffix = currentText.drop(end)

        val lead = when {
            prefix.isEmpty() -> ""
            prefix.endsWith("\n\n") -> ""
            prefix.endsWith("\n") -> "\n"
            else -> "\n\n"
        }

        val headingSnippet = "${lead}## $headingTitle\n\n"
        val newText = prefix + headingSnippet + suffix
        val newCursor = start + headingSnippet.length

        textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    val dayName = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
    }
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
    }

    val contentText = textFieldValue.text
    val wordCount = remember(contentText) {
        if (contentText.isBlank()) 0 else contentText.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(contentText) { contentText.length }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Journal Entry") },
            text = { Text("Are you sure you want to clear your writing for $dayName, $formattedDate?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteJournalEntry(date) {
                            textFieldValue = TextFieldValue("")
                            Toast.makeText(context, "Journal entry deleted", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        }
                    }
                ) {
                    Text("Delete", color = PriorityHighText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInsertBottomSheet) {
        JournalInsertBottomSheet(
            sheetState = insertSheetState,
            onDismissRequest = { showInsertBottomSheet = false },
            onSelectHeading = { template ->
                insertHeadingTemplate(template.title)
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = saveAndExit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = TextPrimary,
                            contentDescription = "Back"
                        )
                    }

                    // Top Header: Day Name + Date
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (contentText.isNotBlank()) {
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    tint = AlertBannerText,
                                    contentDescription = "Delete Journal Entry"
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.saveJournalEntry(date, contentText) {
                                    Toast.makeText(context, "Journal saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Save Journal Entry"
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    // Quick Insert Heading Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // '+' button opening the full insert menu
                        Surface(
                            onClick = { showInsertBottomSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Insert heading",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scrollable quick heading chips
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JournalHeadingTemplate.entries.forEach { template ->
                                Surface(
                                    onClick = { insertHeadingTemplate(template.title) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = template.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = template.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderLight.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Word count and saved status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (journalEntry != null) "Saved" else "Draft",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        Text(
                            text = "$wordCount words • $charCount characters",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .padding(12.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val headingVisualTransformation = remember(primaryColor, textFieldValue.selection) {
                    MarkdownHeadingVisualTransformation(
                        selection = textFieldValue.selection,
                        primaryColor = primaryColor,
                        headingColor = TextPrimary
                    )
                }

                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    visualTransformation = headingVisualTransformation,
                    placeholder = {
                        Text(
                            text = "Write about your day...",
                            color = TextTertiary,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = TextPrimary
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
