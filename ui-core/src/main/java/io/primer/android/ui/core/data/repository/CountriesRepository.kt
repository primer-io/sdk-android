package io.primer.android.ui.core.data.repository

import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.configuration.data.model.CountryCode

interface CountriesRepository {
    suspend fun getCountries(): List<PrimerCountry>

    suspend fun getCountryByCode(code: CountryCode): PrimerCountry

    suspend fun findCountryByQuery(query: String): List<PrimerCountry>

    fun getPhoneCodes(): List<PrimerPhoneCode>

    fun getPhoneCodeByCountryCode(code: CountryCode): PrimerPhoneCode

    fun findPhoneCodeByQuery(query: String): List<PrimerPhoneCode>
}
