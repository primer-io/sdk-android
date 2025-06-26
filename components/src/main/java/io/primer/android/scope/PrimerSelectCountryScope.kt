package io.primer.android.scope

import androidx.compose.runtime.Composable
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's country selection functionality,
 * providing state management, search capabilities, and UI customization for country picker.
 */
interface PrimerSelectCountryScope {

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

    /**
     * Composable function for the entire country selection screen layout.
     */
    var screen: @Composable () -> Unit

    /**
     * Composable function for the search bar with customizable styling and behavior.
     *
     * @param query Current search query
     * @param onQueryChange Callback for search query changes
     * @param placeholder Placeholder text for the search input
     */
    var searchBar: @Composable (query: String, onQueryChange: (String) -> Unit, placeholder: String) -> Unit

    /**
     * Composable function for individual country list items with selection handling.
     *
     * @param country The country to display
     * @param onSelect Callback for when this country item is selected
     */
    var countryItem: @Composable (country: PrimerCountry, onSelect: () -> Unit) -> Unit
}
