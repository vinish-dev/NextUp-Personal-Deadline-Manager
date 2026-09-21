package com.vinish.nextup.ui.quickadd

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.NextUpApplication
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.toTitleCase
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.FabGreen
import com.vinish.nextup.ui.theme.NextUpTheme
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary
import com.vinish.nextup.widget.NextUpWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class QuickAddActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SECTION = "extra_section"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialSection = intent.getStringExtra(EXTRA_SECTION) ?: NextUpWidgetProvider.SECTION_TODAY

        setContent {
            NextUpTheme {
                QuickAddDialogScreen(
                    initialSection = initialSection,
                    onDismiss = { finish() },
                    onSave = { title, section ->
                        saveDeadlineAndFinish(title, section)
                    }
                )
            }
        }
    }

    private fun saveDeadlineAndFinish(title: String, section: String) {
        val today = LocalDate.now()
        val dueDate = when (section) {
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

        val deadline = Deadline(
            title = title.trim().toTitleCase(),
            dueDate = dueDate,
            category = Category.OTHER,
            priority = Priority.MEDIUM,
            isCompleted = false
        )

        val repository = (application as? NextUpApplication)?.repository
        if (repository != null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                repository.insertDeadline(deadline)
                NextUpWidgetProvider.updateAllWidgets(applicationContext)
            }
        }

        Toast.makeText(this, "Deadline added to $section", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@Composable
fun QuickAddDialogScreen(
    initialSection: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf(initialSection) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val sections = listOf(
        NextUpWidgetProvider.SECTION_OVERDUE,
        NextUpWidgetProvider.SECTION_TODAY,
        NextUpWidgetProvider.SECTION_TOMORROW,
        NextUpWidgetProvider.SECTION_THIS_WEEK,
        NextUpWidgetProvider.SECTION_SOMEDAY
    )

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
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
                        text = "Quick Add Deadline",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
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
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = "What needs to be done?",
                            color = TextTertiary,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) isError = false
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(FabGreen),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (title.isBlank()) {
                                    isError = true
                                } else {
                                    onSave(title, selectedSection)
                                }
                            }
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

                // Section Selector Row with Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Due section:",
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
                                .clickable { isDropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSection,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSectionOverdue) Color(0xFFE53935) else Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select section",
                                tint = if (isSectionOverdue) Color(0xFFE53935) else Color(0xFF166534),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
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
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedSection = sectionName
                                        isDropdownExpanded = false
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
                            if (title.isBlank()) {
                                isError = true
                            } else {
                                onSave(title, selectedSection)
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
                            text = "Add Deadline",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
