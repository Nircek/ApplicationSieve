package io.github.nircek.applicationsieve.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.lifecycle.*
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.App
import io.github.nircek.applicationsieve.db.Category
import io.github.nircek.applicationsieve.db.DbRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

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
    }

    fun loadApp(packageName: String) {
        // TODO: firstly look up the db
        loadApp(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA))
    }

    fun randomize() {
        val packages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        val packageInfo = packages.random()
        description.value = "${packages.size} packages. Random:\n${packageInfo.packageName}\n${
            getApplicationName(packageInfo)
        }"
        loadApp(packageInfo)
    }

    fun startApp() {
        val launchIntent =
            if (selectedApp.value != null) pm.getLaunchIntentForPackage(
                selectedApp.value!!
            ) else null
        if (launchIntent != null) app.startActivity(launchIntent)
    }

    fun add() {
        viewModelScope.launch {
            if (selectedApp.value == null) return@launch
            var category = selectedCategory.value!!
            if (category == 0) {
                category = dbRepository.insertCategory(Category("xd")).toInt()
                Toast.makeText(ctx, "Created category with id $category.", Toast.LENGTH_SHORT)
                    .show()
                selectedCategory.value = category
//                Toast.makeText(
//                    ctx,
//                    app.resources.getString(R.string.first_choose_category),
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
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
                val msg = app.resources.getString(R.string.add_message, selectedAppRating.value)
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
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
