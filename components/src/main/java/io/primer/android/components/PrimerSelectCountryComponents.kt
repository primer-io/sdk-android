package io.primer.android.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.presentation.screens.country.DefaultSelectCountryScreen
import io.primer.android.internal.presentation.screens.country.SelectCountryViewModel
import io.primer.android.internal.presentation.screens.country.SelectCountryViewModelFactory
import io.primer.android.internal.presentation.screens.country.components.DefaultCountryItem
import io.primer.android.internal.presentation.screens.country.components.DefaultSearchBar
import io.primer.android.scope.PrimerSelectCountryScope

class PrimerSelectCountryComponents : DISdkComponent {

    @Composable
    fun Screen() {
        screen(viewModel<SelectCountryViewModel>(factory = resolve<SelectCountryViewModelFactory>()))
    }

    /**
     * Composable function for the entire country selection screen layout.
     */
    var screen: @Composable PrimerSelectCountryScope.() -> Unit = {
        DefaultSelectCountryScreen()
    }

    /**
     * Composable function for the search bar with customizable styling and behavior.
     *
     * @param query Current search query
     * @param onQueryChange Callback for search query changes
     * @param placeholder Placeholder text for the search input
     */
    var searchBar: @Composable PrimerSelectCountryScope.(query: String, onQueryChange: (String) -> Unit, placeholder: String) -> Unit =
        { query, onQueryChange, placeholder ->
            DefaultSearchBar(query = query, onQueryChange = onQueryChange, placeholder = placeholder)
        }

    /**
     * Composable function for individual country list items with selection handling.
     *
     * @param country The country to display
     * @param onSelect Callback for when this country item is selected
     */
    var countryItem: @Composable PrimerSelectCountryScope.(country: PrimerCountry, onSelect: () -> Unit) -> Unit =
        { country, onSelect ->
            DefaultCountryItem(country = country, onSelect = onSelect)
        }

}
