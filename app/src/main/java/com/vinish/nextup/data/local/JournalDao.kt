package com.vinish.nextup.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    fun getJournalEntry(date: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getJournalEntryOnce(date: String): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE date = :date")
    suspend fun deleteJournalEntry(date: String)

    @Query("SELECT date FROM journal_entries WHERE LENGTH(TRIM(content)) > 0")
    fun getDatesWithJournalEntries(): Flow<List<String>>

    @Query("SELECT * FROM journal_entries WHERE LENGTH(TRIM(content)) > 0 ORDER BY date DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>
}
