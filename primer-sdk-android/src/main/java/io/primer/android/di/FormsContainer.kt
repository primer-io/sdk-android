package io.primer.android.di

import io.primer.android.data.payments.forms.datasource.LocalFormDataSourceFactory
import io.primer.android.data.payments.forms.repository.FormsDataRepository
import io.primer.android.domain.payments.forms.FormValidationInteractor
import io.primer.android.domain.payments.forms.FormsInteractor
import io.primer.android.domain.payments.forms.repository.FormsRepository
import io.primer.android.domain.payments.forms.validation.ValidatorFactory
import io.primer.android.presentation.payment.forms.FormsViewModelFactory

internal class FormsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            LocalFormDataSourceFactory(sdk.resolve(), sdk.resolve())
        }
        registerSingleton<FormsRepository> {
            FormsDataRepository(
                resolve(),
                sdk.resolve()
            )
        }
        registerSingleton {
            FormsInteractor(
                resolve()
            )
        }

        registerSingleton {
            ValidatorFactory()
        }

        registerSingleton {
            FormValidationInteractor(resolve())
        }

        registerFactory {
            FormsViewModelFactory(
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }
}
