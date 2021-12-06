package com.example.myapplication.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.repositories.CountryRepository

class SettingsViewModel(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    val country = MutableLiveData(countryRepository.getCountry())

    fun setCountry(value: String) {
        countryRepository.setCountry(value)
        country.postValue(countryRepository.getCountry())
    }
}