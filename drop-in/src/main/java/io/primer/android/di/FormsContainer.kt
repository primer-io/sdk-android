package io.primer.android.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.data.payments.forms.datasource.LocalFormDataSourceFactory
import io.primer.android.data.payments.forms.repository.FormsDataRepository
import io.primer.android.domain.payments.forms.FormsInteractor
import io.primer.android.domain.payments.forms.repository.FormsRepository
import io.primer.android.presentation.payment.forms.FormsViewModelFactory

internal class FormsContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton {
            LocalFormDataSourceFactory(primerTheme = sdk().resolve(), countriesRepository = sdk().resolve())
        }
        registerSingleton<FormsRepository> {
            FormsDataRepository(
                factory = resolve(),
            )
        }
        registerSingleton {
            FormsInteractor(
                formsRepository = resolve(),
            )
        }

        registerFactory {
            FormsViewModelFactory(
                formsInteractor = resolve(),
                analyticsInteractor = sdk().resolve(),
            )
        }
    }
}
