package io.primer.android.di

import io.primer.android.domain.helper.CountriesDataRepository
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.fragments.country.SelectCountryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val countriesModule = {
    module {
        single <CountriesRepository> { CountriesDataRepository(get()) }
        viewModel { SelectCountryViewModel(get(), get()) }
    }
}
