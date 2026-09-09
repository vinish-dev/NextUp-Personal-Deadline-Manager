package com.vinish.nextup.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinish.nextup.NextUpApplication
import com.vinish.nextup.data.DeadlineRepository
import com.vinish.nextup.data.local.AppDatabase
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import com.vinish.nextup.model.Subtask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeadlineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeadlineRepository = (application as? NextUpApplication)?.repository
        ?: DeadlineRepository(
            AppDatabase.getDatabase(application).deadlineDao(),
            AppDatabase.getDatabase(application).categoryDao()
        )

    val deadlines: StateFlow<List<Deadline>> = repository.allDeadlines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SampleDeadlines.sampleDeadlines
        )

    val categories: StateFlow<List<Category>> = combine(
        deadlines,
        repository.allCustomCategories
    ) { deadlineList, customCategories ->
        Category.allFrom(deadlineList, customCategories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Category.builtInCategories
    )

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun getDeadline(id: Long): Flow<Deadline?> {
        return repository.getDeadlineById(id)
    }

    fun saveDeadline(deadline: Deadline, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (deadline.id == 0L) {
                repository.insertDeadline(deadline)
            } else {
                repository.updateDeadline(deadline)
            }
            onSaved?.invoke()
        }
    }

    fun addCategory(categoryName: String) {
        val trimmedName = categoryName.trim()
        if (trimmedName.isBlank()) return
        if (Category.builtInCategories.any { it.name.equals(trimmedName, ignoreCase = true) }) return
        viewModelScope.launch {
            repository.addCategory(Category.fromName(trimmedName))
        }
    }

    fun deleteDeadline(id: Long, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteDeadlineById(id)
            onDeleted?.invoke()
        }
    }

    fun toggleCompleted(deadline: Deadline) {
        viewModelScope.launch {
            repository.updateCompletionStatus(deadline.id, !deadline.isCompleted)
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
