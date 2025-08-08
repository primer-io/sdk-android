package io.primer.android.scope

import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.PrimerSelectCountryComponents
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's country selection functionality,
 * providing state management, search capabilities, and UI customization for country picker.
 */
interface PrimerSelectCountryScope : DISdkComponent {

    val components: PrimerSelectCountryComponents
        get() = resolve()

    /**
     * StateFlow representing the current state of country selection,
     * including available countries, filtered results, and search query.
     */
    val state: StateFlow<State>

    /**
     * Handles the selection of a country and returns to the parent scope.
     *
     * @param countryCode The ISO country code of the selected country
     * @param countryName The display name of the selected country
     */
    fun onCountrySelected(countryCode: String, countryName: String)

    /**
     * Cancels the country selection and returns to the parent scope without selection.
     */
    fun onCancel()

    /**
     * Filters the country list based on the provided search query.
     *
     * @param query The search query to filter countries by name or code
     */
    fun onSearch(query: String)

    /**
     * Represents the current state of country selection, including available countries,
     * search functionality, and loading states.
     *
     * @param countries Complete list of available countries
     * @param filteredCountries Filtered list of countries based on search query
     * @param searchQuery Current search query string
     * @param isLoading Whether the country list is being loaded
     */
    data class State(
        val countries: List<PrimerCountry> = emptyList(),
        val filteredCountries: List<PrimerCountry> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
    )
}
