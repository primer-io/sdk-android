package io.primer.sample.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.sample.repositories.CountryRepository

class SettingsViewModelFactory(
    private val countryRepository: CountryRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(
        countryRepository,
    ) as T
}