package com.vinish.nextup.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinish.nextup.NextUpApplication
import com.vinish.nextup.data.DeadlineRepository
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Subtask
import com.vinish.nextup.notification.DeadlineNotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeadlineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeadlineRepository = (application as? NextUpApplication)?.repository
        ?: DeadlineRepository(AppDatabase.getDatabase(application).deadlineDao())

    private val notificationScheduler: DeadlineNotificationScheduler =
        (application as? NextUpApplication)?.notificationScheduler
            ?: DeadlineNotificationScheduler(application)

    private val prefs = application.getSharedPreferences("nextup_preferences", Context.MODE_PRIVATE)

    private val _useSampleData = MutableStateFlow(
        prefs.getBoolean("use_sample_data", false)
    )
    val useSampleData: StateFlow<Boolean> = _useSampleData.asStateFlow()

    private val _sampleDeadlines = MutableStateFlow(SampleDeadlines.sampleDeadlines)

    val deadlines: StateFlow<List<Deadline>> = combine(
        repository.allDeadlines,
        _useSampleData,
        _sampleDeadlines
    ) { roomList, useSample, sampleList ->
        if (useSample) sampleList else roomList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showCompletedInCategories = MutableStateFlow(
        prefs.getBoolean("show_completed_in_categories", false)
    )
    val showCompletedInCategories: StateFlow<Boolean> = _showCompletedInCategories.asStateFlow()

    fun setShowCompletedInCategories(enabled: Boolean) {
        _showCompletedInCategories.value = enabled
        prefs.edit().putBoolean("show_completed_in_categories", enabled).apply()
    }

    fun setUseSampleData(enabled: Boolean) {
        _useSampleData.value = enabled
        prefs.edit().putBoolean("use_sample_data", enabled).apply()
        if (!enabled) {
            // Re-sync all real deadline alarms when switching back to Real Room Data
            viewModelScope.launch {
                try {
                    val roomDeadlines = repository.allDeadlines.first()
                    roomDeadlines.forEach { deadline ->
                        if (!deadline.isCompleted) {
                            notificationScheduler.schedule(deadline)
                        } else {
                            notificationScheduler.cancel(deadline.id)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun getDeadline(id: Long): Flow<Deadline?> {
        return if (_useSampleData.value) {
            _sampleDeadlines.map { list -> list.find { it.id == id } }
        } else {
            repository.getDeadlineById(id)
        }
    }

    fun saveDeadline(deadline: Deadline, onSaved: (() -> Unit)? = null) {
        if (_useSampleData.value) {
            val saved = if (deadline.id == 0L) {
                val newId = (_sampleDeadlines.value.maxOfOrNull { it.id } ?: 0L) + 1L
                deadline.copy(id = newId)
            } else {
                deadline
            }
            if (deadline.id == 0L) {
                _sampleDeadlines.value = _sampleDeadlines.value + saved
            } else {
                _sampleDeadlines.value = _sampleDeadlines.value.map {
                    if (it.id == saved.id) saved else it
                }
            }
            notificationScheduler.schedule(saved)
            onSaved?.invoke()
        } else {
            viewModelScope.launch {
                val saved = if (deadline.id == 0L) {
                    val id = repository.insertDeadline(deadline)
                    deadline.copy(id = id)
                } else {
                    repository.updateDeadline(deadline)
                    deadline
                }
                notificationScheduler.schedule(saved)
                onSaved?.invoke()
            }
        }
    }

    fun deleteDeadline(id: Long, onDeleted: (() -> Unit)? = null) {
        notificationScheduler.cancel(id)
        if (_useSampleData.value) {
            _sampleDeadlines.value = _sampleDeadlines.value.filterNot { it.id == id }
            onDeleted?.invoke()
        } else {
            viewModelScope.launch {
                repository.deleteDeadlineById(id)
                onDeleted?.invoke()
            }
        }
    }

    fun toggleCompleted(deadline: Deadline) {
        val willBeCompleted = !deadline.isCompleted
        if (willBeCompleted) {
            notificationScheduler.cancel(deadline.id)
        } else {
            notificationScheduler.schedule(deadline.copy(isCompleted = false))
        }

        if (_useSampleData.value) {
            _sampleDeadlines.value = _sampleDeadlines.value.map {
                if (it.id == deadline.id) it.copy(isCompleted = willBeCompleted) else it
            }
        } else {
            viewModelScope.launch {
                repository.updateCompletionStatus(deadline.id, willBeCompleted)
            }
        }
    }

    fun toggleSubtask(deadline: Deadline, toggledSubtask: Subtask) {
        val updatedSubtasks = deadline.subtasks.map {
            if (it.id == toggledSubtask.id) it.copy(isCompleted = !it.isCompleted) else it
        }
        if (_useSampleData.value) {
            _sampleDeadlines.value = _sampleDeadlines.value.map {
                if (it.id == deadline.id) it.copy(subtasks = updatedSubtasks) else it
            }
        } else {
            viewModelScope.launch {
                repository.updateDeadline(deadline.copy(subtasks = updatedSubtasks))
            }
        }
    }

    fun addSubtask(deadline: Deadline, title: String) {
        val updatedSubtasks = deadline.subtasks + Subtask(title = title)
        if (_useSampleData.value) {
            _sampleDeadlines.value = _sampleDeadlines.value.map {
                if (it.id == deadline.id) it.copy(subtasks = updatedSubtasks) else it
            }
        } else {
            viewModelScope.launch {
                repository.updateDeadline(deadline.copy(subtasks = updatedSubtasks))
            }
        }
    }
}
