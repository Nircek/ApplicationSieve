package io.github.nircek.applicationsieve

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import io.github.nircek.applicationsieve.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private fun resetIcon() = binding.toolbar.setNavigationIcon(android.R.drawable.ic_dialog_dialer)

    private val menuRoutes =
        mapOf(R.id.nav_package_list to R.id.list, R.id.nav_package_rater to R.id.rater)
    private val routeMenus = menuRoutes.entries.associate { (k, v) -> v to k }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _: NavController, dst: NavDestination, _: Bundle? ->
            resetIcon()
            val to = routeMenus[dst.id]
            if (to != null) binding.navView.setCheckedItem(to)
        }
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navView.setCheckedItem(R.id.nav_package_list)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val to = menuRoutes[menuItem.itemId]
            if (to != null) navController.navigate(to)
            binding.drawerLayout.close()
            true
        }
    }


}
