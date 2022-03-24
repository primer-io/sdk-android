package com.example.myapplication.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.datamodels.AppCountryCode
import com.example.myapplication.repositories.CountryRepository
import com.example.myapplication.ui.CountryItem

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
