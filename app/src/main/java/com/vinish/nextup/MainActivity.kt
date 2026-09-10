package com.vinish.nextup

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.LocalTime
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Priority
import com.vinish.nextup.notifications.DeadlineReminderReceiver
import com.vinish.nextup.ui.theme.NextUpTheme

data class AutofillPayload(
    val title: String,
    val description: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val category: Category? = null,
    val priority: Priority? = null
)

class MainActivity : ComponentActivity() {

    private var targetDeadlineId by mutableStateOf<Long?>(null)
    private var autofillPayload by mutableStateOf<AutofillPayload?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )

        checkNotificationPermission()

        setContent {
            NextUpTheme {
                App(
                    targetDeadlineId = targetDeadlineId,
                    onTargetDeadlineHandled = { targetDeadlineId = null },
                    autofillPayload = autofillPayload,
                    onAutofillPayloadHandled = { autofillPayload = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val id = intent?.getLongExtra(DeadlineReminderReceiver.EXTRA_DEADLINE_ID, -1L) ?: -1L
        if (id != -1L) {
            targetDeadlineId = id
        }

        val autofillTitle = intent?.getStringExtra("autofill_title")
        if (!autofillTitle.isNullOrBlank()) {
            val autofillDesc = intent.getStringExtra("autofill_description").orEmpty()
            val dateStr = intent.getStringExtra("autofill_date")
            val parsedDate = dateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val timeStr = intent.getStringExtra("autofill_time")
            val parsedTime = timeStr?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
            val catStr = intent.getStringExtra("autofill_category")
            val parsedCat = catStr?.let { runCatching { Category.valueOf(it) }.getOrNull() }
            val prioStr = intent.getStringExtra("autofill_priority")
            val parsedPrio = prioStr?.let { runCatching { Priority.valueOf(it) }.getOrNull() }

            autofillPayload = AutofillPayload(
                title = autofillTitle,
                description = autofillDesc,
                date = parsedDate,
                time = parsedTime,
                category = parsedCat,
                priority = parsedPrio
            )
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
