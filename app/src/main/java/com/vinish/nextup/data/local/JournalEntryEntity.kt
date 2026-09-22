package com.vinish.nextup.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey
    val date: String, // ISO-8601 format YYYY-MM-DD
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toLocalDate(): LocalDate = LocalDate.parse(date)

    companion object {
        fun from(date: LocalDate, content: String): JournalEntryEntity {
            return JournalEntryEntity(
                date = date.toString(),
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
