package io.github.nircek.applicationsieve.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        App::class, Category::class, Rating::class
    ], version = 2, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun packageDao(): DbDao

    private class PackageDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    database.packageDao().deleteAllApps() // co to jest? xD
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PackageDatabaseCallback(scope))
                    .build()
                // use `adb shell setprop log.tag.SQLiteStatements VERBOSE` to debug
                INSTANCE = instance
                instance
            }
        }
    }
}
