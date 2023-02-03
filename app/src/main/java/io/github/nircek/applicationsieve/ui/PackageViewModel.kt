package io.github.nircek.applicationsieve.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.lifecycle.*
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.db.Package
import io.github.nircek.applicationsieve.db.PackageRepository
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
    var selectedAppRating = MutableLiveData(0.0f)
    private val app get() = getApplication<Application>()
    private val pm get() = app.packageManager

    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }

    fun loadApp(packageInfo: ApplicationInfo) {
        selectedApp.value = packageInfo.packageName
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
        description.value = "${packages.size} packages. Random: ${packageInfo.packageName}"
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
        viewModelScope.launch {
            repository.insert(
                Package(
                    selectedApp.value!!,
                    PackageRepository.drawableToStream(selectedAppIcon.value!!),
                    selectedAppRating.value!! // why LiveData can be null? maybe it should be StateFlow -- see https://stackoverflow.com/a/64521097/6732111
                )
            )
            val msg = app.resources.getString(R.string.add_message, selectedAppRating.value)
            Toast.makeText(app.applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
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
