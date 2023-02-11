package io.github.nircek.applicationsieve.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DbDao {
    @Query("SELECT * FROM app_table ORDER BY package_name ASC")
    fun getAllApps(): Flow<List<App>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(a: App): Long // SRC: https://stackoverflow.com/a/44364516/6732111

    @Query("SELECT * FROM app_table WHERE package_name = :pkg")
    suspend fun getApp(pkg: String): App?

    @Query("DELETE FROM app_table")
    suspend fun deleteAllApps()

    @Query("SELECT * FROM category_table ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(c: Category): Long

    @Delete
    suspend fun deleteCategory(c: Category)

    @Query("DELETE FROM category_table")
    suspend fun deleteAllCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rating: Rating)

    @Query("SELECT * FROM rating_table r JOIN app_table a ON a.app_id = r.app_id WHERE r.category_id = :category ORDER BY r.rating ASC")
    fun getRatesInCategory(category: Int): Flow<Map<Rating, App>>

    @Query("SELECT * FROM rating_table r JOIN app_table a ON a.app_id = r.app_id ORDER BY r.rating ASC") // TODO: averages
    fun getRatesInAllCategories(): Flow<Map<Rating, App>>
}

