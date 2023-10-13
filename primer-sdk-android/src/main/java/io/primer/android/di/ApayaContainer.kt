package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.datasource.RemoteApayaDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaSessionConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaTokenizationConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationRepository
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.apaya.repository.ApayaSessionRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator

internal class ApayaContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { RemoteApayaDataSource(sdk.resolve()) }

        registerSingleton<ApayaSessionRepository> {
            ApayaSessionDataRepository(
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<ApayaSessionConfigurationRepository> {
            ApayaSessionConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<ApayaTokenizationConfigurationRepository> {
            ApayaTokenizationConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { ApayaSessionParamsValidator() }

        registerSingleton { ApayaWebResultValidator() }

        registerSingleton {
            ApayaSessionInteractor(
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            ApayaSessionConfigurationInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            ApayaTokenizationConfigurationInteractor(
                resolve(),
                sdk.resolve()
            )
        }
    }
}
