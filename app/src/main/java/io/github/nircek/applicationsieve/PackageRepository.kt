package io.github.nircek.applicationsieve

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class PackageRepository(private val packageDao: PackageDao) {

    val allWords: Flow<List<Package>> = packageDao.getAll()

    @WorkerThread
    suspend fun insert(word: Package) {
        packageDao.insert(word)
    }
}

