package io.primer.android.di

import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.data.payments.methods.mapping.PaymentMethodMapping
import io.primer.android.data.payments.methods.repository.PaymentMethodDescriptorsDataRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.payment.billing.DefaultBillingAddressValidator
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry

internal class PaymentMethodDescriptorContainer(private val sdk: SdkContainer) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodCheckerRegistry> { PrimerPaymentMethodCheckerRegistry }

        registerSingleton { PaymentMethodDescriptorFactoryRegistry(resolve()) }

        registerSingleton<PaymentMethodMapping> {
            DefaultPaymentMethodMapping(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { PaymentMethodListFactory(resolve(), sdk.resolve()) }

        registerSingleton<BillingAddressValidator> { DefaultBillingAddressValidator() }

        registerSingleton<PaymentMethodDescriptorsRepository> {
            PaymentMethodDescriptorsDataRepository(
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }
    }
}
