package com.vinish.nextup.data

import com.vinish.nextup.data.local.DeadlineDao
import com.vinish.nextup.data.local.DeadlineEntity
import com.vinish.nextup.data.sample.SampleDeadlines
import com.vinish.nextup.model.Deadline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeadlineRepository(private val deadlineDao: DeadlineDao) {

    val allDeadlines: Flow<List<Deadline>> = deadlineDao.getAllDeadlines().map { list ->
        list.map { it.toDomain() }
    }

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

    suspend fun deleteCompletedDeadlines(): Int {
        return deadlineDao.deleteCompletedDeadlines()
    }

    suspend fun seedIfFirstLaunch(context: android.content.Context) {
        val prefs = context.getSharedPreferences("nextup_prefs", android.content.Context.MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data", false)
        if (!hasSeeded) {
            if (deadlineDao.getDeadlineCount() == 0) {
                val entities = SampleDeadlines.sampleDeadlines.map {
                    DeadlineEntity.fromDomain(it)
                }
                deadlineDao.insertDeadlines(entities)
            }
            prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
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
