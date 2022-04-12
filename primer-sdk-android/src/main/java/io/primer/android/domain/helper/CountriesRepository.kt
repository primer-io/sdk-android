package io.primer.android.domain.helper

import io.primer.android.model.dto.Country
import io.primer.android.model.dto.CountryCode

interface CountriesRepository {

    suspend fun getCountries(): List<Country>

    suspend fun getCountryByCode(code: CountryCode): Country

    suspend fun findCountryByQuery(query: String): List<Country>
}
