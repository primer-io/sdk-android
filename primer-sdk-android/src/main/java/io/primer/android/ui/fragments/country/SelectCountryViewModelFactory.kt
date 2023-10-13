package io.primer.android.ui.fragments.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.helper.CountriesRepository

internal class SelectCountryViewModelFactory(
    private val countriesRepository: CountriesRepository,
    private val analyticsInteractor: AnalyticsInteractor
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SelectCountryViewModel(
            countriesRepository,
            analyticsInteractor
        ) as T
    }
}
