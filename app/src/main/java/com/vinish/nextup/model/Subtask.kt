package com.vinish.nextup.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)
