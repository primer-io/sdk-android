package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.GooglePayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation.GooglePayValidPaymentDataMethodRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation.GooglePayValidationRulesResolver

internal class GooglePayContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<GooglePayConfigurationRepository> {
            GooglePayConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            GooglePayConfigurationInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { GooglePayValidPaymentDataMethodRule() }

        registerSingleton {
            GooglePayValidationRulesResolver(
                resolve()
            )
        }
    }
}
