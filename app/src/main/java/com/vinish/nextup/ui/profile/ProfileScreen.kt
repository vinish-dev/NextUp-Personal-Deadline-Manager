package com.vinish.nextup.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Priority
import com.vinish.nextup.notifications.NotificationCapturePreferences
import com.vinish.nextup.ui.theme.BackgroundLight
import com.vinish.nextup.ui.theme.BorderLight
import com.vinish.nextup.ui.theme.PrimaryBlue
import com.vinish.nextup.ui.theme.PrimaryBlueLight
import com.vinish.nextup.ui.theme.PriorityHighText
import com.vinish.nextup.ui.theme.PriorityLowText
import com.vinish.nextup.ui.theme.PriorityMediumText
import com.vinish.nextup.ui.theme.SurfaceWhite
import com.vinish.nextup.ui.theme.TextPrimary
import com.vinish.nextup.ui.theme.TextSecondary
import com.vinish.nextup.ui.theme.TextTertiary

import androidx.compose.runtime.Immutable

@Immutable
private data class ProfileStats(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val completionRate: Int,
    val highPriorityCount: Int,
    val mediumPriorityCount: Int,
    val lowPriorityCount: Int
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    deadlines: List<Deadline> = SampleDeadlines.sampleDeadlines,
    onClearCompleted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showClearDialog by remember { mutableStateOf(false) }

    var isSmartCaptureEnabled by remember {
        mutableStateOf(NotificationCapturePreferences.isFeatureEnabled(context))
    }
    var isAutoCreateEnabled by remember {
        mutableStateOf(NotificationCapturePreferences.isAutoCreateEnabled(context))
    }
    var hasNotificationAccess by remember {
        mutableStateOf(NotificationCapturePreferences.hasNotificationListenerPermission(context))
    }

    LifecycleResumeEffect(Unit) {
        hasNotificationAccess = NotificationCapturePreferences.hasNotificationListenerPermission(context)
        isSmartCaptureEnabled = NotificationCapturePreferences.isFeatureEnabled(context)
        isAutoCreateEnabled = NotificationCapturePreferences.isAutoCreateEnabled(context)
        onPauseOrDispose { }
    }

    val stats = remember(deadlines) {
        var completed = 0
        var high = 0
        var medium = 0
        var low = 0
        for (d in deadlines) {
            if (d.isCompleted) {
                completed++
            } else {
                when (d.priority) {
                    Priority.HIGH -> high++
                    Priority.MEDIUM -> medium++
                    Priority.LOW -> low++
                }
            }
        }
        val total = deadlines.size
        val pending = total - completed
        val rate = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0
        ProfileStats(
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            completionRate = rate,
            highPriorityCount = high,
            mediumPriorityCount = medium,
            lowPriorityCount = low
        )
    }

    val totalCount = stats.totalCount
    val completedCount = stats.completedCount
    val pendingCount = stats.pendingCount
    val completionRate = stats.completionRate
    val highPriorityCount = stats.highPriorityCount
    val mediumPriorityCount = stats.mediumPriorityCount
    val lowPriorityCount = stats.lowPriorityCount

    val momentumMessage = when {
        totalCount == 0 -> "No deadlines yet — tap + to schedule your first goal."
        pendingCount == 0 -> "All clear! You've completed every scheduled deadline. 🎉"
        highPriorityCount > 0 -> "$highPriorityCount urgent deadline${if (highPriorityCount > 1) "s" else ""} require your attention today."
        completionRate >= 60 -> "High momentum! You're consistently clearing deadlines. 🚀"
        else -> "Steady pace — keep working through your pending list. 🌱"
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear Completed Deadlines?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will remove $completedCount finished deadline${if (completedCount > 1) "s" else ""} from your workspace. Active deadlines won't be touched.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showClearDialog = false
                        onClearCompleted?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityHighText),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All Done", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // Editorial Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text(
                    text = "ACCOUNT & INSIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }
        }

        // User Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vinish",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Personal Workspace",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    // On-device Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryBlueLight,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Private",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Productivity Momentum & Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        text = "Productivity Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Human-focused momentum status
                    Text(
                        text = momentumMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Completion Rate Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Completion Rate",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = "$completionRate%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { completionRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryBlue,
                        trackColor = BorderLight,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = BorderLight)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Row (Total, Done, Pending)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "Total Tasks",
                            value = totalCount.toString(),
                            icon = Icons.Outlined.CheckCircle,
                            iconTint = PrimaryBlue
                        )
                        StatItem(
                            label = "Completed",
                            value = completedCount.toString(),
                            icon = Icons.Outlined.CheckCircle,
                            iconTint = PriorityLowText
                        )
                        StatItem(
                            label = "Pending",
                            value = pendingCount.toString(),
                            icon = Icons.Outlined.PendingActions,
                            iconTint = PriorityHighText
                        )
                    }
                }
            }
        }

        // Active Priority Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        text = "Pending Priorities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PriorityRow(label = "High Priority", count = highPriorityCount, color = PriorityHighText)
                    Spacer(modifier = Modifier.height(10.dp))
                    PriorityRow(label = "Medium Priority", count = mediumPriorityCount, color = PriorityMediumText)
                    Spacer(modifier = Modifier.height(10.dp))
                    PriorityRow(label = "Low Priority", count = lowPriorityCount, color = PriorityLowText)
                }
            }
        }

        // Workspace Management / Declutter Card (Genuinely useful to the user)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        text = "Workspace Maintenance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Completed Deadlines",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (completedCount > 0) "$completedCount completed task${if (completedCount > 1) "s" else ""} ready to archive"
                                else "All clear · No finished tasks to clear",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        if (completedCount > 0) {
                            OutlinedButton(
                                onClick = { showClearDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderLight),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Clean up",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // User Preferences & Privacy (Human-focused, no dev jargon)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        text = "Preferences & Privacy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Deadline Reminders",
                        subtitle = "Timely alerts on due date"
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Smart Notification Capture (Auto-detection like OTP)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Deadline Capture",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (!hasNotificationAccess) "Requires notification permission · Tap to grant"
                                else if (isSmartCaptureEnabled) "Autofills tasks from meetings & apps"
                                else "Paused",
                                fontSize = 12.sp,
                                color = if (!hasNotificationAccess) PriorityHighText else TextSecondary
                            )
                        }
                        if (hasNotificationAccess) {
                            Switch(
                                checked = isSmartCaptureEnabled,
                                onCheckedChange = { isChecked ->
                                    isSmartCaptureEnabled = isChecked
                                    NotificationCapturePreferences.setFeatureEnabled(context, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BorderLight
                                )
                            )
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderLight),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Enable",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    if (hasNotificationAccess && isSmartCaptureEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-save without prompt",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isAutoCreateEnabled) "Saves directly in background" else "Shows OTP-style prompt first",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isAutoCreateEnabled,
                                onCheckedChange = { isChecked ->
                                    isAutoCreateEnabled = isChecked
                                    NotificationCapturePreferences.setAutoCreateEnabled(context, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BorderLight
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsRow(
                        icon = Icons.Outlined.Vibration,
                        title = "Tactile Haptics",
                        subtitle = "Subtle feedback when checking off tasks"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsRow(
                        icon = Icons.Outlined.Shield,
                        title = "Data & Privacy",
                        subtitle = "100% On-Device · No tracking, fully private"
                    )
                }
            }
        }

        // Artisanal Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NextUp • Version 2.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Crafted for focus & personal momentum",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun PriorityRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        Text(
            text = "$count tasks",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
