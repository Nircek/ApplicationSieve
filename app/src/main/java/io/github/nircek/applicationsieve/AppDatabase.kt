package io.github.nircek.applicationsieve

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.nircek.applicationsieve.db.Package
import io.github.nircek.applicationsieve.db.PackageDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Package::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun packageDao(): PackageDao

    private class PackageDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    database.packageDao().deleteAll()
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
                    .addCallback(PackageDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
