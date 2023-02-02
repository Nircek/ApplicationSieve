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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val setOfTopLevelDestinations = setOf(R.id.nav_list, R.id.nav_rater)
        appBarConfiguration =AppBarConfiguration(setOfTopLevelDestinations, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _: NavController, dst: NavDestination, _: Bundle? ->
            binding.navView.setCheckedItem(dst.id)
        }

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            navController.navigate(menuItem.itemId) // FIXME: what if destination is not in menu?
            binding.drawerLayout.close()
            false // will be selected by OnDestinationChangedListener
        }
    }


}
