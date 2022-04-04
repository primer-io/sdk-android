package com.example.myapplication.ui

import com.example.myapplication.datamodels.AppCountryCode

data class CountryItem(
    val countryCode: AppCountryCode,
    var isSelected: Boolean = false
)
