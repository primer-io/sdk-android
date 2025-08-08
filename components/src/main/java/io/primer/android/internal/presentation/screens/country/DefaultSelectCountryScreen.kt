package io.primer.android.internal.presentation.screens.country

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.scope.PrimerSelectCountryScope

@Composable
internal fun PrimerSelectCountryScope.DefaultSelectCountryScreen() {
    val state by state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        CheckoutAppBar(
            title = stringResource(R.string.primer_components_select_country),
            onBackClick = { onCancel() },
        )

        with(components) {
            searchBar(
                state.searchQuery,
                { query -> onSearch(query) },
                stringResource(R.string.primer_components_card_form_placeholder_search_countries),
            )
        }


        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.filteredCountries) { country ->
                    with(components) {
                        countryItem(country) {
                            onCountrySelected(country.code.name, country.name)
                        }
                    }
                }
            }
        }
    }
}
