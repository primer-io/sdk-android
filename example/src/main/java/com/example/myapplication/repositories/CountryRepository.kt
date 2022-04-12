package com.example.myapplication.repositories

import com.example.myapplication.datamodels.AppCountryCode
import com.example.myapplication.datasources.CountryDataSource

class CountryRepository(
    private val datasource: CountryDataSource,
) {

    fun getCountry(): AppCountryCode = datasource.getCountry()

    fun getCurrency(): String = datasource.getCountry().currencyCode.name

    fun setCountry(value: AppCountryCode) { datasource.setCountry(value) }

    fun getCountries(): List<AppCountryCode> = datasource.getCountries()
}
