package com.example.myapplication.datasources

import com.example.myapplication.datamodels.AppCountryCode

class CountryDataSource(
    private var country: AppCountryCode,
) {

    fun getCountry() = country

    fun setCountry(value: AppCountryCode) { country = value }

    fun getCountries(): List<AppCountryCode> = AppCountryCode.values().toList()
}
