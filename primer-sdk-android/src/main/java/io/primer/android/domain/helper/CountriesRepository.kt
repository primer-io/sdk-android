package io.primer.android.domain.helper

import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.domain.action.models.PrimerCountry

interface CountriesRepository {

    suspend fun getCountries(): List<PrimerCountry>

    suspend fun getCountryByCode(code: CountryCode): PrimerCountry

    suspend fun findCountryByQuery(query: String): List<PrimerCountry>
}
