package com.vinish.nextup.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinish.nextup.NextUpApplication
import com.vinish.nextup.data.DeadlineRepository
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Subtask
import com.vinish.nextup.notifications.DeadlineNotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeadlineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeadlineRepository = (application as? NextUpApplication)?.repository
        ?: DeadlineRepository(AppDatabase.getDatabase(application).deadlineDao())

    val deadlines: StateFlow<List<Deadline>> = repository.allDeadlines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedIfFirstLaunch(application)
        }
    }

    fun getDeadline(id: Long): Flow<Deadline?> {
        return repository.getDeadlineById(id)
    }

    fun saveDeadline(deadline: Deadline, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            val deadlineId = if (deadline.id == 0L) {
                repository.insertDeadline(deadline)
            } else {
                repository.updateDeadline(deadline)
                deadline.id
            }

            val savedDeadline = deadline.copy(id = deadlineId)
            DeadlineNotificationScheduler.scheduleReminder(getApplication(), savedDeadline)

            onSaved?.invoke()
        }
    }

    fun deleteDeadline(id: Long, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            DeadlineNotificationScheduler.cancelReminder(getApplication(), id)
            repository.deleteDeadlineById(id)
            onDeleted?.invoke()
        }
    }

    fun toggleCompleted(deadline: Deadline) {
        viewModelScope.launch {
            val newStatus = !deadline.isCompleted
            repository.updateCompletionStatus(deadline.id, newStatus)
            val updatedDeadline = deadline.copy(isCompleted = newStatus)
            if (newStatus) {
                DeadlineNotificationScheduler.cancelReminder(getApplication(), deadline.id)
            } else {
                DeadlineNotificationScheduler.scheduleReminder(getApplication(), updatedDeadline)
            }
        }
    }

    fun clearCompletedDeadlines(onCleared: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.deleteCompletedDeadlines()
            onCleared?.invoke(count)
        }
    }

    fun toggleSubtask(deadline: Deadline, toggledSubtask: Subtask) {
        viewModelScope.launch {
            val updatedSubtasks = deadline.subtasks.map {
                if (it.id == toggledSubtask.id) it.copy(isCompleted = !it.isCompleted) else it
            }
            repository.updateDeadline(deadline.copy(subtasks = updatedSubtasks))
        }
    }

    fun addSubtask(deadline: Deadline, title: String) {
        viewModelScope.launch {
            val updatedSubtasks = deadline.subtasks + Subtask(title = title)
            repository.updateDeadline(deadline.copy(subtasks = updatedSubtasks))
        }
    }
}
