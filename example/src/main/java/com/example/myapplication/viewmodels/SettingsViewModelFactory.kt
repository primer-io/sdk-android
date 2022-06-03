package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.repositories.CountryRepository

class SettingsViewModelFactory(
    private val countryRepository: CountryRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(
        countryRepository,
    ) as T
}