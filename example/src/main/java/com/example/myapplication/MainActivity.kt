package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.datamodels.AppCountryCode
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.datasources.CountryDataSource
import com.example.myapplication.repositories.AppApiKeyRepository
import com.example.myapplication.repositories.CountryRepository
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.MainViewModelFactory
import com.example.myapplication.viewmodels.SettingsViewModel
import com.example.myapplication.viewmodels.SettingsViewModelFactory
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private val apiKeyDataSource = AppApiKeyRepository()

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

        val countryDataSource = CountryDataSource(AppCountryCode.TH)
        val countryRepository = CountryRepository(countryDataSource)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(WeakReference(this), countryRepository, apiKeyDataSource),
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
