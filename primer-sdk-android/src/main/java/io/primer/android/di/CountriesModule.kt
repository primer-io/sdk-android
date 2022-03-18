package io.primer.android.di

import io.primer.android.domain.helper.CountriesDataRepository
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.fragments.country.SelectCountryViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.lang.ref.WeakReference

internal val countriesModule = {
    module {
        single <CountriesRepository> { CountriesDataRepository(WeakReference(get())) }
        viewModel { SelectCountryViewModel(get(), get()) }
    }
}
