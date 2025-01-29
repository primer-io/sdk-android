package io.primer.sample.datasources

import io.primer.sample.datamodels.AppCountryCode

class CountryDataSource(
    private var country: AppCountryCode,
) {

    fun getCountry() = country

    fun setCountry(value: AppCountryCode) {
        country = value
    }

    fun getCountries(): List<AppCountryCode> = AppCountryCode.entries.toList()
}
