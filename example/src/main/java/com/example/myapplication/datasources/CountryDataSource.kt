package com.example.myapplication.datasources

class CountryDataSource(
    private var country: String,
) {

    fun getCountry() = country

    fun setCountry(value: String) { country = value }

}