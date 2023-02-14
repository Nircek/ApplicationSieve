package io.github.nircek.applicationsieve.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.App
import io.github.nircek.applicationsieve.db.Category
import io.github.nircek.applicationsieve.db.DbRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection


@ExperimentalCoroutinesApi
class PackageViewModel(private val dbRepository: DbRepository, application: Application) :
    AndroidViewModel(application) {

    var description = MutableLiveData(app.getString(R.string.app_name))
    var selectedApp = MutableLiveData<String?>(null)
    var selectedAppName = MutableLiveData<String?>(null)
    var selectedAppIcon = MutableLiveData<Drawable?>(null)
    var selectedAppRating = MutableLiveData(0.0f)
    var selectedCategory = MutableLiveData(0)
    val listInSelectedCategory = selectedCategory.asFlow()
        .flatMapLatest { dbRepository.getRatedApps(it) }
        .asLiveData()

    private fun query(url: String) = selectedApp.asFlow().flatMapLatest {
        flow {
            emit(0)
            val status = withContext(Dispatchers.IO) { return@withContext getStatus("$url$it") }
            emit(if (status == 200) 1 else -1)
        }
    }.asLiveData()

    val google = query("https://play.google.com/store/apps/details?id=")
    val fdroid = query("https://f-droid.org/en/packages/")

    val listCategories = dbRepository.allCategories.asLiveData()
    private val app get() = getApplication<Application>()
    private val pm get() = app.packageManager
    private val ctx get() = app.applicationContext
    private fun getApplicationName(info: ApplicationInfo) =
        // SRC: https://stackoverflow.com/a/15114434/6732111
        pm.getApplicationLabel(info).toString()

    fun dropApps() = viewModelScope.launch { dbRepository.dropApps() }
    fun dropCategories() = viewModelScope.launch { dbRepository.dropCategories() }

    fun loadApp(packageInfo: ApplicationInfo) {
        selectedApp.value = packageInfo.packageName
        selectedAppName.value = getApplicationName(packageInfo)
        selectedAppIcon.value = packageInfo.loadIcon(pm)
        description.value = getAppInfo(packageInfo)
    }

    fun loadApp(packageName: String) {
        // TODO: firstly look up the db
        loadApp(pm.getApplicationInfo(packageName, 0))
    }

    fun randomize() {
        val packages = pm.getInstalledApplications(0).filter { it.flags and FLAG_SYSTEM == 0 }
        val packageInfo = packages.random()
        loadApp(packageInfo)
        description.value = "${packages.size} packages. Random:\n${getAppInfo(packageInfo)}"
    }

    private fun parseFlags(flags: Int): String {
        return ((if (flags and FLAG_ALLOW_BACKUP != 0) listOf("ALLOW_BACKUP") else listOf()) +
                (if (flags and FLAG_ALLOW_CLEAR_USER_DATA != 0) listOf("ALLOW_CLEAR_USER_DATA") else listOf()) +
                (if (flags and FLAG_ALLOW_TASK_REPARENTING != 0) listOf("ALLOW_TASK_REPARENTING") else listOf()) +
                (if (flags and FLAG_DEBUGGABLE != 0) listOf("DEBUGGABLE") else listOf()) +
                (if (flags and FLAG_EXTERNAL_STORAGE != 0) listOf("EXTERNAL_STORAGE") else listOf()) +
                (if (flags and FLAG_FACTORY_TEST != 0) listOf("FACTORY_TEST") else listOf()) +
                (if (flags and FLAG_HAS_CODE != 0) listOf("HAS_CODE") else listOf()) +
                (if (flags and FLAG_KILL_AFTER_RESTORE != 0) listOf("KILL_AFTER_RESTORE") else listOf()) +
                (if (flags and FLAG_LARGE_HEAP != 0) listOf("LARGE_HEAP") else listOf()) +
                (if (flags and FLAG_PERSISTENT != 0) listOf("PERSISTENT") else listOf()) +
                (if (flags and FLAG_RESIZEABLE_FOR_SCREENS != 0) listOf("RESIZEABLE_FOR_SCREENS") else listOf()) +
                (if (flags and FLAG_RESTORE_ANY_VERSION != 0) listOf("RESTORE_ANY_VERSION") else listOf()) +
                (if (flags and FLAG_STOPPED != 0) listOf("STOPPED") else listOf()) +
                (if (flags and FLAG_SUPPORTS_LARGE_SCREENS != 0) listOf("SUPPORTS_LARGE_SCREENS") else listOf()) +
                (if (flags and FLAG_SUPPORTS_NORMAL_SCREENS != 0) listOf("SUPPORTS_NORMAL_SCREENS") else listOf()) +
                (if (flags and FLAG_SUPPORTS_SMALL_SCREENS != 0) listOf("SUPPORTS_SMALL_SCREENS") else listOf()) +
                (if (flags and FLAG_SUPPORTS_XLARGE_SCREENS != 0) listOf("SUPPORTS_XLARGE_SCREENS") else listOf()) +
                (if (flags and FLAG_SYSTEM != 0) listOf("SYSTEM") else listOf()) +
                (if (flags and FLAG_TEST_ONLY != 0) listOf("TEST_ONLY") else listOf()) +
                (if (flags and FLAG_UPDATED_SYSTEM_APP != 0) listOf("UPDATED_SYSTEM_APP") else listOf()) +
                (if (flags and FLAG_VM_SAFE_MODE != 0) listOf("VM_SAFE_MODE") else listOf()) +
                // 17
                (if (flags and FLAG_INSTALLED != 0) listOf("INSTALLED") else listOf()) +
                (if (flags and FLAG_IS_DATA_ONLY != 0) listOf("IS_DATA_ONLY") else listOf()) +
                (if (flags and FLAG_SUPPORTS_RTL != 0) listOf("SUPPORTS_RTL") else listOf()) +

                // 21
                (if (flags and FLAG_FULL_BACKUP_ONLY != 0) listOf("FULL_BACKUP_ONLY") else listOf()) +
                (if (flags and FLAG_MULTIARCH != 0) listOf("MULTIARCH") else listOf()) +

                // 23
                (if (flags and FLAG_EXTRACT_NATIVE_LIBS != 0) listOf("EXTRACT_NATIVE_LIBS") else listOf()) +
                (if (flags and FLAG_HARDWARE_ACCELERATED != 0) listOf("HARDWARE_ACCELERATED") else listOf()) +
                (if (flags and FLAG_USES_CLEARTEXT_TRAFFIC != 0) listOf("USES_CLEARTEXT_TRAFFIC") else listOf()) +

                // 24
                (if (flags and FLAG_SUSPENDED != 0) listOf("SUSPENDED") else listOf()))
            .joinToString("|")
    }

    private fun getAppInfo(info: ApplicationInfo): String {
        val pkg = info.packageName
        val pkgInfo = pm.getPackageInfo(pkg, 0)
        val ret = mutableListOf<String>()
        ret += "$pkg v${pkgInfo.versionName}"
        ret += getApplicationName(info)
        ret += "flags: ${parseFlags(info.flags)}"

        ret += "${getFriendlySource(info)} ${getFriendlyDate(pkgInfo.firstInstallTime)} " +
                "(${getFriendlyDate(pkgInfo.lastUpdateTime)})"

        val tar = info.targetSdkVersion
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) info.minSdkVersion else null
        val com =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) info.compileSdkVersion else null
        ret += "tar ${getFullAndroidVersion(tar)}"
        ret += "min ${getFullAndroidVersion(min)}"
        ret += "com ${getFullAndroidVersion(com)}"

        return ret.joinToString("\n")
    }

    private fun getFriendlyDate(e: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(e))

    private fun getAndroidCodename(sdk: Int): String = Build.VERSION_CODES::class.java.fields
        .map { f -> Pair(f.name, f.getInt(Any())) }
        .filter { e -> e.second == sdk }.getOrNull(0)?.first ?: "API$sdk"

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
        if (sdk == null) null else "$sdk/${getAndroidCodename(sdk)}/Android${
            getFriendlyAndroidVersion(sdk)
        }"

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

    fun startApp() {
        val launchIntent =
            if (selectedApp.value != null) pm.getLaunchIntentForPackage(
                selectedApp.value!!
            ) else null
        if (launchIntent != null) app.startActivity(launchIntent)
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

    fun add() {
        viewModelScope.launch {
            if (selectedApp.value == null) return@launch
            val category = selectedCategory.value!!
            if (category == 0) {
                Toast.makeText(
                    ctx,
                    app.resources.getString(R.string.first_choose_category),
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            viewModelScope.launch {
                dbRepository.rate(
                    App(
                        selectedApp.value!!,
                        selectedAppName.value!!,
                        DbRepository.drawableToStream(selectedAppIcon.value!!),
                    ), category,
                    selectedAppRating.value!! // why LiveData can be null? maybe it should be StateFlow -- see https://stackoverflow.com/a/64521097/6732111
                )
                val msg = app.resources.getString(R.string.rate_app_msg, selectedAppRating.value)
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            selectedCategory.value = dbRepository.insertCategory(Category(name)).toInt()
        }
    }

    fun deleteCategory(c: Category) {
        viewModelScope.launch {
            dbRepository.deleteCategory(c)
        }
    }

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
