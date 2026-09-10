package com.vinish.nextup.data

import com.vinish.nextup.data.local.CategoryDao
import com.vinish.nextup.data.local.CategoryEntity
import com.vinish.nextup.data.local.DeadlineDao
import com.vinish.nextup.data.local.DeadlineEntity
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Category
import com.vinish.nextup.model.Deadline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DeadlineRepository(
    private val deadlineDao: DeadlineDao,
    private val categoryDao: CategoryDao? = null
) {

    val allDeadlines: Flow<List<Deadline>> = deadlineDao.getAllDeadlines().map { list ->
        list.map { it.toDomain() }
    }

    val allCustomCategories: Flow<List<Category>> = categoryDao?.getAllCategories()?.map { list ->
        list.map { entity -> Category.fromName(entity.name) }
    } ?: flowOf(emptyList())

    fun getDeadlineById(id: Long): Flow<Deadline?> {
        return deadlineDao.getDeadlineById(id).map { it?.toDomain() }
    }

    suspend fun getDeadlineByIdOnce(id: Long): Deadline? {
        return deadlineDao.getDeadlineByIdOnce(id)?.toDomain()
    }

    suspend fun insertDeadline(deadline: Deadline): Long {
        return deadlineDao.insertDeadline(DeadlineEntity.fromDomain(deadline))
    }

    suspend fun updateDeadline(deadline: Deadline) {
        deadlineDao.updateDeadline(DeadlineEntity.fromDomain(deadline))
    }

    suspend fun deleteDeadline(deadline: Deadline) {
        deadlineDao.deleteDeadline(DeadlineEntity.fromDomain(deadline))
    }

    suspend fun deleteDeadlineById(id: Long) {
        deadlineDao.deleteDeadlineById(id)
    }

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) {
        deadlineDao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun addCategory(category: Category) {
        val trimmedName = category.name.trim()
        if (categoryDao == null || trimmedName.isBlank()) return
        if (Category.builtInCategories.any { it.name.equals(trimmedName, ignoreCase = true) }) return
        if (categoryDao.categoryExists(trimmedName) == 0) {
            categoryDao.insertCategory(CategoryEntity(trimmedName))
        }
    }

    suspend fun seedIfEmpty() {
        if (deadlineDao.getDeadlineCount() == 0) {
            val entities = SampleDeadlines.sampleDeadlines.map {
                DeadlineEntity.fromDomain(it)
            }
            deadlineDao.insertDeadlines(entities)
        }
    }
}
