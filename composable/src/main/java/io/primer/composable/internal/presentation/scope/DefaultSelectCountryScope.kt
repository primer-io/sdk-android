package io.primer.composable.internal.presentation.scope

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.composable.internal.presentation.screens.country.DefaultSelectCountryScreen
import io.primer.composable.internal.presentation.screens.country.components.DefaultCountryItem
import io.primer.composable.internal.presentation.screens.country.components.DefaultSearchBar
import io.primer.composable.scope.PrimerSelectCountryScope

internal abstract class DefaultSelectCountryScope : ViewModel(), PrimerSelectCountryScope {

    override var screen: @Composable PrimerSelectCountryScope.() -> Unit = {
        DefaultSelectCountryScreen()
    }

    override var searchBar: @Composable (query: String, onQueryChange: (String) -> Unit, placeholder: String) -> Unit = { query, onQueryChange, placeholder ->
        DefaultSearchBar(query = query, onQueryChange = onQueryChange, placeholder = placeholder)
    }

    override var countryItem: @Composable (country: PrimerCountry, onSelect: () -> Unit) -> Unit = { country, onSelect ->
        DefaultCountryItem(country = country, onSelect = onSelect)
    }
}
