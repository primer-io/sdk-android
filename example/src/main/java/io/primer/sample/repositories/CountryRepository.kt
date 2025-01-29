package io.primer.sample.repositories

import io.primer.sample.datamodels.AppCountryCode
import io.primer.sample.datasources.CountryDataSource


class CountryRepository(
    private val datasource: CountryDataSource,
) {

    fun getCountry(): AppCountryCode = datasource.getCountry()

    fun getCurrency(): String = datasource.getCountry().currencyCode.name

    fun setCountry(value: AppCountryCode) {
        datasource.setCountry(value)
    }

    fun getCountries(): List<AppCountryCode> = datasource.getCountries()
}
