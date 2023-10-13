package io.primer.android.di

import io.primer.android.domain.helper.CountriesDataRepository
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.fragments.country.SelectCountryViewModelFactory

internal class CountriesDataStorageContainer(private val sdk: SdkContainer) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<CountriesRepository> { CountriesDataRepository(sdk.resolve()) }

        registerFactory {
            SelectCountryViewModelFactory(resolve(), sdk.resolve())
        }
    }
}
