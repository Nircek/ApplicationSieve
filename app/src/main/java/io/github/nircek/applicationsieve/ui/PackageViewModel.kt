package io.github.nircek.applicationsieve.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.App
import io.github.nircek.applicationsieve.db.Category
import io.github.nircek.applicationsieve.db.DbRepository
import io.github.nircek.applicationsieve.util.toLiveFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.math.max


@ExperimentalCoroutinesApi
class PackageViewModel(private val dbRepository: DbRepository, application: Application) :
    AndroidViewModel(application) {

    //#region helpers
    private fun <T> Flow<T>.toStateFlow() = stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private fun <T> Flow<T>.toStateFlow(init: T) =
        stateIn(viewModelScope, SharingStarted.Eagerly, init)

    private fun <T> Flow<T>.toLiveStateFlow() = toStateFlow().toLiveFlow()
    private fun <T> Flow<T>.toLiveStateFlow(init: T) = toStateFlow(init).toLiveFlow()
    //#endregion

    //#region
    fun <T> throwTo(hopper: MutableSharedFlow<T>, what: T) =
        viewModelScope.launch { hopper.emit(what) }

    fun <T> MutableSharedFlow<T>.throwIn(what: T) = throwTo(this, what)
    val hopCategory = MutableSharedFlow<String>()
    //#endregion

    //#region state holders
    val pkgName = MutableStateFlow<String?>(null)
    val appRate = MutableStateFlow(0.0f)
    val selCategory = MutableStateFlow(0).toLiveFlow()
    //#endregion

    //#region flows
    // FIXME: firstly look up the db
    val appInfo = pkgName.mapLatest { it?.let { pm.getApplicationInfo(it, 0) } }
    val pkgInfo = pkgName.mapLatest { it?.let { pm.getPackageInfo(it, 0) } }
    val appName = appInfo.mapLatest { it?.let { getApplicationName(it) } }.toStateFlow()
    val appIcon = appInfo.mapLatest { it?.loadIcon(pm) }.toStateFlow()
    val appVersion = pkgInfo.mapLatest { it?.versionName }.toLiveFlow()
    val appFlags = appInfo.mapLatest { it?.let { parseFlags(it.flags) } }.toLiveFlow()
    val appTarSdk = appInfo.mapLatest { it?.let { it.targetSdkVersion } }.toStateFlow()
    val appMinSdk =
        appInfo.mapLatest { it?.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) it.minSdkVersion else null } }
            .toStateFlow()
    val appComSdk =
        appInfo.mapLatest { it?.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it.compileSdkVersion else null } }
            .toStateFlow()

    // TODO: add preferred sdk version format (M/23/6.0/6.0(23))
    val appFriendlyTarSdk = appTarSdk.mapLatest { getFullAndroidVersion(it) }.toLiveFlow()
    val appFriendlyMinSdk = appMinSdk.mapLatest { getFullAndroidVersion(it) }.toLiveFlow()
    val appFriendlyComSdk = appComSdk.mapLatest { getFullAndroidVersion(it) }.toLiveFlow()
    val appInstallTime = pkgInfo.mapLatest { it?.firstInstallTime }.toStateFlow()
    val appUpdateTime = pkgInfo.mapLatest { it?.lastUpdateTime }.toStateFlow()
    val appFriendlyInstallTime =
        appInstallTime.mapLatest { it?.let { getFriendlyDate(it) } }.toLiveFlow()
    val appFriendlyUpdateTime =
        appUpdateTime.mapLatest { it?.let { getFriendlyDate(it) } }.toLiveFlow()
    val appSource = appInfo.mapLatest { it?.let { getFriendlySource(it) } }.toLiveStateFlow()
    val appGoogleRepo = query("https://play.google.com/store/apps/details?id=").toLiveFlow()
    val appFdroidRepo = query("https://f-droid.org/en/packages/").toLiveFlow()
    val appsInCategory = selCategory.flatMapLatest { dbRepository.getRatedApps(it) }.toLiveFlow()
    val allCategories = dbRepository.allCategories.toLiveStateFlow(listOf())


    private fun query(url: String) = pkgName.flatMapLatest {
        flow {
            emit(0) // TODO: cache
            val status = withContext(Dispatchers.IO) { return@withContext getStatus("$url$it") }
            emit(if (status == 200) 1 else -1)
        }
    }

    //#endregion flows

    //#region android
    private val app get() = getApplication<Application>()
    private val pm get() = app.packageManager
    private val ctx get() = app.applicationContext

    // TODO: make it a cached flow?
    private fun getRandomInstalledPkg() = pm.getInstalledApplications(0)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .random().packageName

    private fun getApplicationName(info: ApplicationInfo) = pm.getApplicationLabel(info).toString()

    private fun parseFlags(flags: Int) = ApplicationInfo::class.java.fields
        .mapNotNull {
            if (!it.name.startsWith("FLAG_")) return@mapNotNull null
            val rev = it.name.startsWith("FLAG_SUPPORTS_")
            val contain = flags and it.getInt(ApplicationInfo()) != 0
            if (contain == rev) return@mapNotNull null
            return@mapNotNull "${if (rev) "~" else ""}${it.name}".replace("FLAG_", "")
        }.joinToString("|")

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }

    private fun getFriendlyDate(e: Long) = sdf.format(Date(e))

    private fun getAndroidCodename(sdk: Int): String = Build.VERSION_CODES::class.java.fields
        .filter { it.getInt(Any()) == sdk }.getOrNull(0)?.name ?: "API$sdk"

    private fun getFriendlyAndroidVersion(sdk: Int) = when (sdk) {
        in 1..4 -> 1
        in 5..10 -> 2
        in 11..13 -> 3
        in 14..20 -> 4
        in 21..22 -> 5
        23 -> 6
        in 24..25 -> 7
        in 26..27 -> 8
        28 -> 9
        29 -> 10
        30 -> 11
        in 31..32 -> 12
        33 -> 13
        34 -> 14
        else -> null
    }?.toString() ?: getAndroidCodename(sdk)

    private fun getFullAndroidVersion(sdk: Int?) =
        sdk?.let { "$sdk/${getAndroidCodename(sdk)}/Android${getFriendlyAndroidVersion(sdk)}" }

    private fun getFriendlySource(info: ApplicationInfo): String {
        val (who, via) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            pm.getInstallSourceInfo(info.packageName)
                .let { Pair(it.installingPackageName, it.initiatingPackageName) }
        else Pair(pm.getInstallerPackageName(info.packageName), null)
        return when (who) {
            "com.android.vending" -> "G"
            "org.fdroid.fdroid" -> "F"
            "com.aurora.store" -> "A"
            "com.huawei.appmarket" -> "H"
            "com.google.android.packageinstaller", null -> "C"
            else -> "U[$who]".also { Log.d(javaClass.simpleName, "unknown vendor: ($who, $via)") }
        }
    }

    private fun getStatus(url: String): Int {
        return try {
            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.connect()
            conn.responseCode
        } catch (e: IOException) {
            // e.printStackTrace()
            -1
        }
    }
    //#endregion

    //#region actions
    fun loadApp(packageName: String) {
        pkgName.value = packageName
    }

    fun randomize() = loadApp(getRandomInstalledPkg()) // TODO: how many packages
    fun startApp() =
        pkgName.value?.let { pm.getLaunchIntentForPackage(it) }?.let { app.startActivity(it) }

    fun add() {
        viewModelScope.launch {
            val category = selCategory.value
            if (category == 0) {
                Toast.makeText(
                    ctx,
                    app.resources.getString(R.string.first_choose_category),
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            viewModelScope.launch {
                pkgName.value?.let {
                    val a = App(
                        it,
                        appName.value!!,
                        DbRepository.drawableToStream(appIcon.value!!),
                        appSource.value!!,
                        appInstallTime.value!!,
                        appUpdateTime.value!!,
                        appMinSdk.value!!,
                        max(appTarSdk.value!!, appComSdk.value!!)
                    )
                    dbRepository.rate(a, category, appRate.value)
                    val msg =
                        app.resources.getString(R.string.rate_app_msg, appRate.value)
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun addCategory(name: String) = viewModelScope.launch {
        selCategory.value = dbRepository.insertCategory(Category(name)).toInt()
    }

    fun deleteCategory(c: Category) = viewModelScope.launch { dbRepository.deleteCategory(c) }

    fun dropApps() = viewModelScope.launch { dbRepository.dropApps() }
    fun dropCategories() = viewModelScope.launch { dbRepository.dropCategories() }
    //#endregion
}

@OptIn(ExperimentalCoroutinesApi::class)
class PackageViewModelFactory(
    private val dbRepository: DbRepository,
    private val app: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PackageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PackageViewModel(dbRepository, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
