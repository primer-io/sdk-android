package io.primer.android.di

import io.primer.android.data.currencyformat.datasource.LocalCurrencyFormatDataSource
import io.primer.android.data.currencyformat.datasource.RemoteCurrencyFormatDataSource
import io.primer.android.data.currencyformat.repository.CurrencyFormatDataRepository
import io.primer.android.domain.currencyformat.interactors.FetchCurrencyFormatDataInteractor
import io.primer.android.domain.currencyformat.interactors.FormatAmountToCurrencyInteractor
import io.primer.android.domain.currencyformat.interactors.FormatAmountToDecimalInteractor
import io.primer.android.domain.currencyformat.repository.CurrencyFormatRepository

internal class CurrencyFormatContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { LocalCurrencyFormatDataSource(sdk.resolve()) }

        registerSingleton { RemoteCurrencyFormatDataSource(sdk.resolve()) }

        registerSingleton<CurrencyFormatRepository> {
            CurrencyFormatDataRepository(
                sdk.resolve(),
                resolve(),
                resolve()
            )
        }

        registerSingleton {
            FetchCurrencyFormatDataInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { FormatAmountToCurrencyInteractor(resolve(), sdk.resolve()) }

        registerSingleton { FormatAmountToDecimalInteractor(resolve(), sdk.resolve()) }
    }
}
