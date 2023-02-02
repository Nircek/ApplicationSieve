package io.github.nircek.applicationsieve

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PackageViewModel(private val repository: PackageRepository, application: Application) :
    AndroidViewModel(application) {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPkgs: LiveData<List<Package>> = repository.allPkgs.asLiveData()

    var description = MutableLiveData<String>(app.getString(R.string.app_name))
    var selectedApp = MutableLiveData<String?>(null)
    var selectedAppIcon = MutableLiveData<Drawable?>(null)
    var selectedAppIconStream = MutableLiveData<ByteArray?>(null)
    var selectedAppRating = MutableLiveData(0.0f)
    private val app get() = getApplication<Application>()
    private val pm get() = app.packageManager

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    private fun insert(pkg: Package) = viewModelScope.launch {
        repository.insert(pkg)
    }

    fun loadApp(packageInfo: ApplicationInfo) {
        // TODO: make it take Package as argument
        selectedApp.value = packageInfo.packageName
        selectedAppIcon.value = packageInfo.loadIcon(pm)
        selectedAppIconStream.value = PackageRepository.drawableToStream(selectedAppIcon.value!!)

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
        description.value =
            "${packages.size} packages. Random: ${packageInfo.packageName} ${packageInfo.sourceDir} ${
                pm.getLaunchIntentForPackage(packageInfo.packageName)
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
        if (selectedApp.value == null) return
        Toast.makeText(app.applicationContext, "${selectedAppRating.value}/7", Toast.LENGTH_SHORT)
            .show()
        insert(
            Package(
                selectedApp.value!!,
                selectedAppIconStream.value!!,
                selectedAppRating.value!! // why LiveData can be null? maybe it should be StateFlow -- see https://stackoverflow.com/a/64521097/6732111
            )
        )

    }

}

class PackageViewModelFactory(
    private val repository: PackageRepository,
    private val app: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PackageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PackageViewModel(repository, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
