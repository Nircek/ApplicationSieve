package io.github.nircek.applicationsieve

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {

    @Query("SELECT * FROM package_table ORDER BY package ASC")
    fun getAll(): Flow<List<Package>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(`package`: Package)

    @Query("DELETE FROM package_table")
    suspend fun deleteAll()

}

