package io.primer.android.internal.presentation.screens.country

import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.ui.core.data.repository.CountriesDataRepository
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.scope.DefaultSelectCountryScope
import io.primer.android.scope.PrimerSelectCountryScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SelectCountryViewModel : DefaultSelectCountryScope(), DISdkComponent {

    private val countriesRepository: CountriesDataRepository by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    private val _state = MutableStateFlow(PrimerSelectCountryScope.State())
    override val state: StateFlow<PrimerSelectCountryScope.State> = _state.asStateFlow()

    init { loadCountries() }

    override fun onCountrySelected(countryCode: String, countryName: String) {
        viewModelScope.launch {
            checkoutNavigator.navigateBackWithResult("selected_country", Pair(countryCode, countryName))
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.navigateBack()
        }
    }

    override fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterCountries(query)
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val countries = countriesRepository.getCountries()
                _state.update {
                    it.copy(
                        countries = countries,
                        filteredCountries = countries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun filterCountries(query: String) {
        viewModelScope.launch {
            val filteredList = if (query.isBlank()) {
                _state.value.countries
            } else {
                countriesRepository.findCountryByQuery(query)
            }
            _state.update { it.copy(filteredCountries = filteredList) }
        }
    }
}
