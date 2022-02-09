package com.example.myapplication.repositories

import com.example.myapplication.datasources.CountryDataSource

class CountryRepository(
    private val datasource: CountryDataSource,
) {

    fun getCountry(): String = datasource.getCountry()

    fun getCurrency(): String = datasource.getCountry().let { country ->
        when (country) {
            "DE" -> "EUR"
            "SG" -> "SGD"
            "SE" -> "SEK"
            "NO" -> "NOK"
            "US" -> "USD"
            "GB" -> "GBP"
            "NL" -> "EUR"
            "PL" -> "PLN"
            else -> "EUR"
        }
    }

    fun setCountry(value: String) { datasource.setCountry(value) }
}