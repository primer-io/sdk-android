package io.primer.composable.internal.presentation.screens.country

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
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.scope.PrimerSelectCountryScope

@Composable
internal fun PrimerSelectCountryScope.DefaultSelectCountryScreen() {
    val state by state.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CheckoutAppBar(
            title = "Select Country",
            onBackClick = { onCancel() }
        )
        
        searchBar(state.searchQuery, { query -> onSearch(query) }, "Search countries...")
        
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.filteredCountries) { country ->
                    countryItem(country) { onCountrySelected(country.code.name, country.name) }
                }
            }
        }
    }
}