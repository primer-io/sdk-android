package io.primer.android.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.domain.helper.CountriesDataRepository
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.fragments.country.SelectCountryViewModelFactory

internal class CountriesDataStorageContainer(private val sdk: () -> SdkContainer) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<CountriesRepository> { CountriesDataRepository(context = sdk().resolve()) }

        registerFactory {
            SelectCountryViewModelFactory(countriesRepository = resolve(), analyticsInteractor = sdk().resolve())
        }
    }
}
