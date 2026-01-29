package io.primer.android.internal.presentation.screens.country

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.internal.presentation.screens.country.components.DefaultCountryItem
import io.primer.android.internal.presentation.screens.country.components.DefaultSearchBar
import io.primer.android.scope.LocalCountrySelectionScope

@Composable
internal fun DefaultCountrySelection() {
    val scope = LocalCountrySelectionScope.current as CountrySelectionViewModel
    val state by scope.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        DefaultSearchBar()

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.filteredCountries) { country ->
                    DefaultCountryItem(country)
                }
            }
        }
    }
}
