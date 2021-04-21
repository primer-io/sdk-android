package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class PaymentMethodDescriptorFactoryRegistry(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistry,
) {

    private val factories: MutableMap<String, PaymentMethodDescriptorFactory> = mutableMapOf()

    fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
    ): PaymentMethodDescriptor? =
        factories[paymentMethodRemoteConfig.type]?.create(
            checkoutConfig = checkoutConfig,
            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
            paymentMethod = paymentMethod,
            paymentMethodCheckers = paymentMethodCheckers
        )

    fun register(id: String, factory: PaymentMethodDescriptorFactory) {
        factories[id] = factory
    }

    fun unregister(id: PaymentMethodDescriptorFactory) {
        factories.remove(id)
    }

    operator fun get(id: String): PaymentMethodDescriptorFactory? = factories[id]
}
