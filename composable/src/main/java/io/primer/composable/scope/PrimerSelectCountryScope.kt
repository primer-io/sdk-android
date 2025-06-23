package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import kotlinx.coroutines.flow.StateFlow

interface PrimerSelectCountryScope {

    val state: StateFlow<State>

    fun onCountrySelected(countryCode: String, countryName: String)
    fun onCancel()
    fun onSearch(query: String)

    data class State(
        val countries: List<PrimerCountry> = emptyList(),
        val filteredCountries: List<PrimerCountry> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false
    )

    // UI Customization
    var screen: @Composable PrimerSelectCountryScope.() -> Unit
    var searchBar: @Composable (query: String, onQueryChange: (String) -> Unit, placeholder: String) -> Unit
    var countryItem: @Composable (country: PrimerCountry, onSelect: () -> Unit) -> Unit
}