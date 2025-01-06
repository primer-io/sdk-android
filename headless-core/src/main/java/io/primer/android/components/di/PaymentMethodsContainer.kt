package io.primer.android.components.di

import io.primer.android.components.implementation.domain.PaymentMethodModulesInteractor
import io.primer.android.components.implementation.errors.data.mapper.PaymentMethodsErrorMapper
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PrimerPaymentMethodCheckerRegistry

internal class PaymentMethodsContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { PrimerPaymentMethodCheckerRegistry }

        registerSingleton {
            PaymentMethodModulesInteractor(
                paymentMethodDescriptorsRepository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                config = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                checkoutErrorHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        sdk().resolve<ErrorMapperRegistry>().register(PaymentMethodsErrorMapper())
    }
}
