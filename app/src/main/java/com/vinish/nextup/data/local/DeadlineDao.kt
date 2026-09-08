package com.vinish.nextup.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeadlineDao {
    @Query("SELECT * FROM deadlines ORDER BY dueDate ASC, dueTime ASC")
    fun getAllDeadlines(): Flow<List<DeadlineEntity>>

    @Query("SELECT * FROM deadlines WHERE id = :id")
    fun getDeadlineById(id: Long): Flow<DeadlineEntity?>

    @Query("SELECT * FROM deadlines WHERE id = :id")
    suspend fun getDeadlineByIdOnce(id: Long): DeadlineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadline(deadline: DeadlineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadlines(deadlines: List<DeadlineEntity>)

    @Update
    suspend fun updateDeadline(deadline: DeadlineEntity)

    @Delete
    suspend fun deleteDeadline(deadline: DeadlineEntity)

    @Query("DELETE FROM deadlines WHERE id = :id")
    suspend fun deleteDeadlineById(id: Long)

    @Query("UPDATE deadlines SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM deadlines")
    suspend fun getDeadlineCount(): Int
}
