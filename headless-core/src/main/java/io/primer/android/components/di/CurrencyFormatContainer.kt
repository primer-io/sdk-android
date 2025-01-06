package io.primer.android.components.di

import io.primer.android.components.currencyformat.data.CurrencyFormatDataRepository
import io.primer.android.components.currencyformat.data.datasource.LocalCurrencyFormatDataSource
import io.primer.android.components.currencyformat.data.datasource.RemoteCurrencyFormatDataSource
import io.primer.android.components.currencyformat.domain.interactors.FetchCurrencyFormatDataInteractor
import io.primer.android.components.currencyformat.domain.interactors.FormatAmountToDecimalInteractor
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.components.currencyformat.domain.repository.CurrencyFormatRepository
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataWithInputProvider
import io.primer.android.data.settings.internal.MonetaryAmount

internal class CurrencyFormatContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { LocalCurrencyFormatDataSource(sdk().resolve()) }

        registerSingleton { RemoteCurrencyFormatDataSource(sdk().resolve()) }

        registerSingleton<CurrencyFormatRepository> {
            CurrencyFormatDataRepository(
                sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                resolve(),
                resolve(),
            )
        }

        registerSingleton {
            FetchCurrencyFormatDataInteractor(
                resolve(),
                sdk().resolve(),
            )
        }

        registerSingleton { FormatAmountToDecimalInteractor(resolve(), sdk().resolve()) }

        registerFactory(name = "FORMATTED_AMOUNT_PROVIDER") {
            BaseDataWithInputProvider<MonetaryAmount, String> { monetaryAmount ->
                resolve<FormatAmountToDecimalInteractor>().invoke(
                    FormatCurrencyParams(monetaryAmount),
                )
            }
        }
    }
}
