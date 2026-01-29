package io.primer.android.scope

import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import kotlinx.coroutines.flow.StateFlow

val LocalCountrySelectionScope = staticCompositionLocalOf<PrimerCountrySelectionScope> {
    error("PrimerCountrySelectionScope not provided.")
}

interface PrimerCountrySelectionScope {

    val state: StateFlow<State>

    fun onCountrySelected(countryCode: String, countryName: String)

    fun onSearch(query: String)

    data class State(
        val countries: List<PrimerCountry> = emptyList(),
        val filteredCountries: List<PrimerCountry> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
    )
}
