package io.primer.android.internal.presentation.screens.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.usecase.GetCountriesUseCase
import io.primer.android.internal.navigation.CountryNavigator
import io.primer.android.scope.PrimerCountrySelectionScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CountrySelectionViewModel(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val logReporter: LogReporter,
    private val countryNavigator: CountryNavigator,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel(), PrimerCountrySelectionScope {

    private val _state = MutableStateFlow(PrimerCountrySelectionScope.State())
    override val state: StateFlow<PrimerCountrySelectionScope.State> = _state.asStateFlow()

    init {
        logReporter.info("Initializing country selection", component = TAG)
        loadCountries()
    }

    override fun onCountrySelected(countryCode: String, countryName: String) {
        logReporter.debug("Country selected: code=$countryCode", component = TAG)
        countryNavigator.onCountrySelected(countryCode, countryName)
    }

    override fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterCountries(query)
    }

    private fun loadCountries() {
        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isLoading = true) }
            getCountriesUseCase()
                .onSuccess { countries ->
                    logReporter.debug("Countries loaded: ${countries.size}", component = TAG)
                    _state.update {
                        it.copy(
                            countries = countries,
                            filteredCountries = countries,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    logReporter.error(
                        "Failed to load countries: ${error.message}",
                        component = TAG,
                        throwable = error,
                    )
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun filterCountries(query: String) {
        viewModelScope.launch(ioDispatcher) {
            val filteredList = if (query.isBlank()) {
                _state.value.countries
            } else {
                getCountriesUseCase.findByQuery(query)
            }
            _state.update { it.copy(filteredCountries = filteredList) }
        }
    }

    companion object {
        private const val TAG = "CountrySelectionViewModel"
    }
}
