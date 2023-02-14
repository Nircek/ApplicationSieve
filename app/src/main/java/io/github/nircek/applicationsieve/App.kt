package io.github.nircek.applicationsieve

import android.app.Application
import io.github.nircek.applicationsieve.db.AppDatabase
import io.github.nircek.applicationsieve.db.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val dbRepository by lazy { DbRepository(database.packageDao()) }
}
