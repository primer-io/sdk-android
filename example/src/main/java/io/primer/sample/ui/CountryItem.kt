package io.primer.sample.ui

import io.primer.sample.datamodels.AppCountryCode

data class CountryItem(
    val countryCode: AppCountryCode,
    var isSelected: Boolean = false
)
