package io.primer.android.internal.domain.usecase

import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.ui.core.data.repository.CountriesDataRepository

internal class GetCountriesUseCase(
    private val countriesRepository: CountriesDataRepository,
) {

    suspend operator fun invoke(): Result<List<PrimerCountry>> = runCatching {
        countriesRepository.getCountries()
    }

    suspend fun findByQuery(query: String): List<PrimerCountry> {
        return countriesRepository.findCountryByQuery(query)
    }
}
