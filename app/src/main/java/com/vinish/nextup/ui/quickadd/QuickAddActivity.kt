package com.vinish.nextup.ui.quickadd

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.vinish.nextup.MainActivity
import com.vinish.nextup.NextUpApplication
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase
import com.vinish.nextup.notification.NotificationHelper
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.FabGreen
import com.vinish.nextup.ui.theme.NextUpTheme
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import com.vinish.nextup.widget.NextUpWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import kotlin.time.Duration.Companion.milliseconds

fun parseQuickAddInput(rawInput: String): Pair<String, String?> {
    val lines = rawInput.lines()
    val title = lines.firstOrNull()?.trim().orEmpty()
    val description = if (lines.size > 1) {
        val remaining = lines.drop(1).joinToString("\n").trim()
        remaining.ifEmpty { null }
    } else {
        null
    }
    return Pair(title, description)
}

object QuickAddVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val newlineIndex = rawText.indexOf('\n')

        val builder = AnnotatedString.Builder()
        if (newlineIndex == -1) {
            builder.append(rawText)
            builder.addStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = TextPrimary
                ),
                0,
                rawText.length
            )
        } else {
            builder.append(rawText)
            builder.addStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = TextPrimary
                ),
                0,
                newlineIndex
            )
            builder.addStyle(
                SpanStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = TextSecondary
                ),
                newlineIndex + 1,
                rawText.length
            )
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

class QuickAddActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SECTION = "extra_section"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val initialSectionFromIntent = intent.getStringExtra(EXTRA_SECTION)

        setContent {
            NextUpTheme {
                var existingDeadline by remember { mutableStateOf<Deadline?>(null) }
                var initialInput by remember { mutableStateOf("") }
                var initialCategory by remember { mutableStateOf(Category.OTHER) }
                var initialSection by remember {
                    mutableStateOf(initialSectionFromIntent ?: NextUpWidgetProvider.SECTION_TODAY)
                }
                var isReady by remember { mutableStateOf(taskId == -1L) }

                LaunchedEffect(taskId) {
                    if (taskId != -1L) {
                        val repo = (application as? NextUpApplication)?.repository
                        val deadline = withContext(Dispatchers.IO) {
                            repo?.getDeadlineByIdOnce(taskId)
                        }
                        if (deadline != null) {
                            existingDeadline = deadline
                            initialInput = if (deadline.description.isNullOrBlank()) {
                                deadline.title
                            } else {
                                "${deadline.title}\n${deadline.description}"
                            }
                            initialCategory = deadline.category
                            initialSection = initialSectionFromIntent ?: getSectionForDeadline(deadline.dueDate, deadline.dueTime)
                        }
                        isReady = true
                    }
                }

                if (isReady) {
                    QuickAddDialogScreen(
                        initialInput = initialInput,
                        initialSection = initialSection,
                        initialCategory = initialCategory,
                        isEditMode = taskId != -1L && existingDeadline != null,
                        onDismiss = { finish() },
                        onSave = { title, description, section, category ->
                            val currentDeadline = existingDeadline
                            if (currentDeadline != null) {
                                updateDeadlineAndFinish(currentDeadline, title, description, section, category)
                            } else {
                                saveDeadlineAndFinish(title, description, section, category)
                            }
                        },
                        onDelete = if (existingDeadline != null) {
                            { deleteDeadlineAndFinish(existingDeadline!!) }
                        } else null,
                        onOpenInApp = if (existingDeadline != null) {
                            {
                                val mainIntent = Intent(this@QuickAddActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra(NotificationHelper.EXTRA_DEADLINE_ID, existingDeadline!!.id)
                                    putExtra(MainActivity.EXTRA_OPEN_EDIT, true)
                                }
                                startActivity(mainIntent)
                                finish()
                            }
                        } else null
                    )
                }
            }
        }
    }

    private fun getSectionForDeadline(dueDate: LocalDate, dueTime: LocalTime?): String {
        val today = LocalDate.now()
        val nowTime = LocalTime.now()
        val tomorrow = today.plusDays(1)
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return when {
            dueDate.isBefore(today) || (dueDate.isEqual(today) && dueTime != null && dueTime.isBefore(nowTime)) ->
                NextUpWidgetProvider.SECTION_OVERDUE
            dueDate.isEqual(today) -> NextUpWidgetProvider.SECTION_TODAY
            dueDate.isEqual(tomorrow) -> NextUpWidgetProvider.SECTION_TOMORROW
            dueDate.isAfter(tomorrow) && !dueDate.isAfter(endOfWeek) -> NextUpWidgetProvider.SECTION_THIS_WEEK
            else -> NextUpWidgetProvider.SECTION_SOMEDAY
        }
    }

    private fun calculateDueDateForSection(section: String): LocalDate {
        val today = LocalDate.now()
        return when (section) {
            NextUpWidgetProvider.SECTION_OVERDUE -> today.minusDays(1)
            NextUpWidgetProvider.SECTION_TODAY -> today
            NextUpWidgetProvider.SECTION_TOMORROW -> today.plusDays(1)
            NextUpWidgetProvider.SECTION_THIS_WEEK -> {
                if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(7)
                else today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            }
            NextUpWidgetProvider.SECTION_SOMEDAY -> today.plusMonths(1)
            else -> today
        }
    }

    private fun saveDeadlineAndFinish(
        title: String,
        description: String?,
        section: String,
        category: Category
    ) {
        val dueDate = calculateDueDateForSection(section)
        val deadline = Deadline(
            title = title.trim().toTitleCase(),
            description = description?.trim()?.ifEmpty { null }?.toSentenceCase(),
            dueDate = dueDate,
            category = category,
            priority = Priority.MEDIUM,
            reminder = Reminder.AT_TIME,
            isCompleted = false
        )

        val app = application as? NextUpApplication
        val repository = app?.repository
        val scheduler = app?.notificationScheduler
        if (repository != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val id = repository.insertDeadline(deadline)
                scheduler?.schedule(deadline.copy(id = id))
                NextUpWidgetProvider.updateAllWidgets(applicationContext)
            }
        }

        Toast.makeText(this, "Deadline added to $section", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateDeadlineAndFinish(
        existingDeadline: Deadline,
        title: String,
        description: String?,
        section: String,
        category: Category
    ) {
        val originalSection = getSectionForDeadline(existingDeadline.dueDate, existingDeadline.dueTime)
        val targetDueDate = if (section == originalSection) {
            existingDeadline.dueDate
        } else {
            calculateDueDateForSection(section)
        }

        val updated = existingDeadline.copy(
            title = title.trim().toTitleCase(),
            description = description?.trim()?.ifEmpty { null }?.toSentenceCase(),
            dueDate = targetDueDate,
            category = category
        )

        val app = application as? NextUpApplication
        val repository = app?.repository
        val scheduler = app?.notificationScheduler
        if (repository != null) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updateDeadline(updated)
                scheduler?.schedule(updated)
                NextUpWidgetProvider.updateAllWidgets(applicationContext)
            }
        }

        Toast.makeText(this, "Deadline updated", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteDeadlineAndFinish(deadline: Deadline) {
        val app = application as? NextUpApplication
        val repository = app?.repository
        val scheduler = app?.notificationScheduler
        if (repository != null) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.deleteDeadlineById(deadline.id)
                scheduler?.cancel(deadline.id)
                NextUpWidgetProvider.updateAllWidgets(applicationContext)
            }
        }

        Toast.makeText(this, "Deadline deleted", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@Composable
fun QuickAddDialogScreen(
    initialInput: String = "",
    initialSection: String,
    initialCategory: Category = Category.OTHER,
    categories: List<Category> = Category.builtInCategories,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String?, String, Category) -> Unit,
    onDelete: (() -> Unit)? = null,
    onOpenInApp: (() -> Unit)? = null
) {
    var textFieldValue by remember(initialInput) {
        mutableStateOf(TextFieldValue(initialInput, TextRange(initialInput.length)))
    }
    var selectedSection by remember(initialSection) { mutableStateOf(initialSection) }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var isSectionDropdownExpanded by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val sections = listOf(
        NextUpWidgetProvider.SECTION_OVERDUE,
        NextUpWidgetProvider.SECTION_TODAY,
        NextUpWidgetProvider.SECTION_TOMORROW,
        NextUpWidgetProvider.SECTION_THIS_WEEK,
        NextUpWidgetProvider.SECTION_SOMEDAY
    )

    // Close any open popup on back press without closing the activity
    BackHandler(enabled = isCategoryDropdownExpanded || isSectionDropdownExpanded) {
        isCategoryDropdownExpanded = false
        isSectionDropdownExpanded = false
    }

    // Immediately request focus and show software keyboard so layout does not shift after delay
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Deadline?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this deadline? This action cannot be undone.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Outer dismiss overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dialog Card
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click inside card */ }
                ),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Edit Deadline" else "Quick Add Deadline",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditMode && onOpenInApp != null) {
                            IconButton(
                                onClick = onOpenInApp,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = "Open in app edit mode",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (isEditMode && onDelete != null) {
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete deadline",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Description Multiline Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAF8))
                        .border(
                            1.dp,
                            if (isError) MaterialTheme.colorScheme.error else BorderLight,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (textFieldValue.text.isEmpty()) {
                        Column {
                            Text(
                                text = "What needs to be done?",
                                color = TextTertiary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Add description on next line (optional)",
                                color = TextTertiary.copy(alpha = 0.7f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                            val (title, _) = parseQuickAddInput(it.text)
                            if (title.isNotBlank()) isError = false
                        },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 6,
                        visualTransformation = QuickAddVisualTransformation,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(FabGreen),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = true,
                            imeAction = ImeAction.Default
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                if (isError) {
                    Text(
                        text = "Title cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Category:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(selectedCategory.iconBackground)
                                .border(
                                    1.dp,
                                    selectedCategory.iconTint.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isSectionDropdownExpanded = false
                                    isCategoryDropdownExpanded = true
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(selectedCategory.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedCategory.icon,
                                    contentDescription = selectedCategory.displayName(),
                                    tint = selectedCategory.iconTint,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedCategory.displayName(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedCategory.iconTint
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select category",
                                tint = selectedCategory.iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            properties = PopupProperties(
                                focusable = false,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            ),
                            modifier = Modifier
                                .background(SurfaceWhite)
                                .heightIn(max = 240.dp)
                        ) {
                            categories.forEach { category ->
                                val isSelected = category.matches(selectedCategory)
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(category.iconBackground),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = category.displayName(),
                                                tint = category.iconTint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = category.displayName(),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) category.iconTint else TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryDropdownExpanded = false
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date / Section Selector Row with Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Due date:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Box {
                        val isSectionOverdue = selectedSection == NextUpWidgetProvider.SECTION_OVERDUE
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSectionOverdue) Color(0xFFFFEBEE) else Color(0xFFF0FDF4))
                                .border(
                                    1.dp,
                                    if (isSectionOverdue) Color(0xFFFFCDD2) else Color(0xFFDCFCE7),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isCategoryDropdownExpanded = false
                                    isSectionDropdownExpanded = true
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = if (isSectionOverdue) Color(0xFFE53935) else Color(0xFF166534),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedSection,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSectionOverdue) Color(0xFFE53935) else Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select due date",
                                tint = if (isSectionOverdue) Color(0xFFE53935) else Color(0xFF166534),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isSectionDropdownExpanded,
                            onDismissRequest = { isSectionDropdownExpanded = false },
                            properties = PopupProperties(
                                focusable = false,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            ),
                            modifier = Modifier
                                .background(SurfaceWhite)
                                .heightIn(max = 240.dp)
                        ) {
                            sections.forEach { sectionName ->
                                val isOverdueOption = sectionName == NextUpWidgetProvider.SECTION_OVERDUE
                                val isSelected = sectionName == selectedSection
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sectionName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected && isOverdueOption -> Color(0xFFE53935)
                                                isSelected -> FabGreen
                                                isOverdueOption -> Color(0xFFE53935)
                                                else -> TextPrimary
                                            },
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        selectedSection = sectionName
                                        isSectionDropdownExpanded = false
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val (title, description) = parseQuickAddInput(textFieldValue.text)
                            if (title.isBlank()) {
                                isError = true
                            } else {
                                onSave(title, description, selectedSection, selectedCategory)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FabGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "Save Changes" else "Add Deadline",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
