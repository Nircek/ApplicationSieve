package io.github.nircek.applicationsieve.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DbDao {
    @Query("SELECT * FROM app_table ORDER BY package_name ASC")
    fun getAllApps(): Flow<List<App>>

    @Query("SELECT package_name FROM app_table JOIN rating_table ON app_table.app_id == rating_table.app_id ORDER BY package_name ASC")
    fun getAllRatedPackageNames(): Flow<List<String>>

    @Insert
    suspend fun _insertApp(a: App): Long // SRC: https://stackoverflow.com/a/44364516/6732111

    @Upsert
    /** @param a needs to have correct app_id */
    suspend fun _upsertApp(a: App)

    @Transaction
    suspend fun addApp(app: App): Int {
        val savedApp = getApp(app.package_name) ?: return _insertApp(app).toInt()
        app.app_id = savedApp.app_id
        _upsertApp(app)
        return savedApp.app_id
    }

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

    @Query("SELECT a.app_id, r.category_id, a.package_name, r.rating_time, r.version, r.versionCode, a.app_name, a.icon, r.description, r.rating FROM rating_table r JOIN app_table a ON a.app_id = r.app_id WHERE a.package_name = :pkgName AND r.category_id = :category")
    suspend fun getRatedApp(pkgName: String, category: Int): DbRepository.RatedApp?

    @Query("SELECT a.app_id, r.category_id, a.package_name, r.rating_time, r.version, r.versionCode, a.app_name, a.icon, r.description, r.rating FROM rating_table r JOIN app_table a ON a.app_id = r.app_id WHERE a.package_name = :pkgName")
    suspend fun getRatesOfApp(pkgName: String): List<DbRepository.RatedApp>
}

