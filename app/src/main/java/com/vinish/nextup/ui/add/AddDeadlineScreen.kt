package com.vinish.nextup.ui.add

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.model.Recurrence
import com.vinish.nextup.model.Reminder
import com.vinish.nextup.ui.add.components.AddTopBar
import com.vinish.nextup.ui.add.components.CategorySelector
import com.vinish.nextup.ui.add.components.DateTimeSelector
import com.vinish.nextup.ui.add.components.DeadlineTextField
import com.vinish.nextup.ui.add.components.PrioritySelector
import com.vinish.nextup.ui.add.components.RecurrenceSelector
import com.vinish.nextup.ui.add.components.ReminderSelector
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.PrimaryBlue
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddDeadlineScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveDeadline: ((Deadline) -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.EDUCATION) }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var reminder by remember { mutableStateOf(Reminder.NONE) }
    var recurrence by remember { mutableStateOf(Recurrence.NONE) }

    var titleError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            AddTopBar(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title (Required)
            DeadlineTextField(
                label = "Title",
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                placeholder = "e.g. Car Insurance Renewal",
                singleLine = true,
                isError = titleError,
                errorMessage = "Title is required"
            )

            // Description (Optional)
            DeadlineTextField(
                label = "Description (optional)",
                value = description,
                onValueChange = { description = it },
                placeholder = "Add details...",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )

            // Category Selector
            CategorySelector(
                selectedCategory = category,
                onCategorySelected = { category = it }
            )

            // Due Date and Optional Due Time
            DateTimeSelector(
                selectedDate = dueDate,
                onDateSelected = { dueDate = it },
                selectedTime = dueTime,
                onTimeSelected = { dueTime = it }
            )

            // Priority Selector (Directly selectable Low/Medium/High)
            PrioritySelector(
                selectedPriority = priority,
                onPrioritySelected = { priority = it }
            )

            // Reminder Selector
            ReminderSelector(
                selectedReminder = reminder,
                onReminderSelected = { reminder = it }
            )

            // Recurrence / Repeat Selector
            RecurrenceSelector(
                selectedRecurrence = recurrence,
                onRecurrenceSelected = { recurrence = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Add/Save Deadline Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        val newDeadline = Deadline(
                            title = title.trim(),
                            description = description.trim().ifEmpty { null },
                            dueDate = dueDate,
                            dueTime = dueTime,
                            category = category,
                            priority = priority,
                            reminder = reminder,
                            recurrence = recurrence,
                            isCompleted = false
                        )
                        Log.d("AddDeadlineScreen", "Created deadline: $newDeadline")
                        onSaveDeadline?.invoke(newDeadline)
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Add Deadline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddDeadlineScreenPreview() {
    AddDeadlineScreen()
}