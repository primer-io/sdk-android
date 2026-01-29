package io.primer.android.internal.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Navigator for country selection flow.
 * Handles navigation to country picker and receiving selection results.
 */
internal interface CountryNavigator {
    /** Flow of navigation requests to country selection screen. */
    val navigationRequests: SharedFlow<Unit>

    /** Flow of country selection results. */
    val countrySelectionResult: SharedFlow<CountrySelectionResult>

    /** Request navigation to country selection screen. */
    fun navigateToCountrySelection()

    /** Called when a country is selected. */
    fun onCountrySelected(code: String, name: String)

    data class CountrySelectionResult(val code: String, val name: String)
}

/**
 * Default implementation of [CountryNavigator].
 */
internal class DefaultCountryNavigator : CountryNavigator {
    private val _navigationRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val navigationRequests: SharedFlow<Unit> = _navigationRequests.asSharedFlow()

    private val _countrySelectionResult = MutableSharedFlow<CountryNavigator.CountrySelectionResult>(
        extraBufferCapacity = 1,
    )
    override val countrySelectionResult: SharedFlow<CountryNavigator.CountrySelectionResult> =
        _countrySelectionResult.asSharedFlow()

    override fun navigateToCountrySelection() {
        _navigationRequests.tryEmit(Unit)
    }

    override fun onCountrySelected(code: String, name: String) {
        _countrySelectionResult.tryEmit(CountryNavigator.CountrySelectionResult(code, name))
    }
}
