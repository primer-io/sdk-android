package com.example.myapplication

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.datasources.CountryDataSource
import com.example.myapplication.repositories.CountryRepository
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.MainViewModelFactory
import com.example.myapplication.viewmodels.SettingsViewModel
import com.example.myapplication.viewmodels.SettingsViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val countryDataSource = CountryDataSource("DE")
        val countryRepository = CountryRepository(countryDataSource)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(countryRepository),
        ).get(MainViewModel::class.java)

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(countryRepository),
        ).get(SettingsViewModel::class.java)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
            || super.onSupportNavigateUp()
    }
}
