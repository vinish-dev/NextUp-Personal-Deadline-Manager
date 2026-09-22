package com.vinish.nextup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinish.nextup.notification.NotificationHelper
import com.vinish.nextup.ui.DeadlineViewModel
import com.vinish.nextup.ui.theme.AppTheme
import com.vinish.nextup.ui.theme.NextUpTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_EDIT = "extra_open_edit"
    }

    private val viewModel: DeadlineViewModel by viewModels()
    private var targetDeadlineId by mutableStateOf<Long?>(null)
    private var openInEditMode by mutableStateOf(false)

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // User responded to POST_NOTIFICATIONS request
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)
        requestNotificationPermission()

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
        setContent {
            val themeName by viewModel.appTheme.collectAsStateWithLifecycle()
            val currentTheme = if (themeName == "blue") AppTheme.BLUE else AppTheme.GREEN
            NextUpTheme(appTheme = currentTheme) {
                App(
                    initialDeadlineId = targetDeadlineId,
                    openInEditMode = openInEditMode,
                    viewModel = viewModel
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
        val deadlineId = intent?.getLongExtra(NotificationHelper.EXTRA_DEADLINE_ID, -1L) ?: -1L
        if (deadlineId != -1L) {
            targetDeadlineId = deadlineId
            openInEditMode = intent?.getBooleanExtra(EXTRA_OPEN_EDIT, false) ?: false
        }
    }

    private fun requestNotificationPermission() {
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
