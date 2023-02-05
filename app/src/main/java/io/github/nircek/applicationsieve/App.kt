package io.github.nircek.applicationsieve

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import io.github.nircek.applicationsieve.db.AppDatabase
import io.github.nircek.applicationsieve.db.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    // .detectDiskReads().detectDiskWrites()
                    .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) detectUnbufferedIo() }
                    .detectResourceMismatches()
                    .detectCustomSlowCalls()
                    .detectNetwork()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectCleartextNetwork()
                    .detectFileUriExposure()
                    // .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyDeathOnCleartextNetwork()
                    .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) penaltyDeathOnFileUriExposure() }
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this
                            .detectContentUriWithoutPermission()
                            .detectUntaggedSockets()
                    }
                    // .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) detectNonSdkApiUsage() }
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) this
                            .detectCredentialProtectedWhileLocked()
                            .detectImplicitDirectBoot()
                    }
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this
                            // .detectIncorrectContextUse()
                            .detectUnsafeIntentLaunch()
                    }
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
    }

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val dbRepository by lazy { DbRepository(database.packageDao()) }
}
