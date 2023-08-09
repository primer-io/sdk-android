package io.primer.sample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.sample.datamodels.AppCountryCode
import io.primer.sample.repositories.CountryRepository
import io.primer.sample.ui.CountryItem

class SettingsViewModel(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    val country = MutableLiveData(countryRepository.getCountry())
    val countries = MutableLiveData(countryRepository.getCountries().map { CountryItem(it) })

    fun setCountry(value: AppCountryCode) {
        countryRepository.setCountry(value)
        country.postValue(countryRepository.getCountry())
    }
}
