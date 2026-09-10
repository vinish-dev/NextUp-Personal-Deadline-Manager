package com.vinish.nextup

import android.app.Application
import com.vinish.nextup.data.DeadlineRepository
import com.vinish.nextup.data.local.AppDatabase

class NextUpApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { DeadlineRepository(database.deadlineDao(), database.categoryDao()) }
}
