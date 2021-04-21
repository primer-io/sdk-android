package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

// TODO rename to PaymentMethodDescriptorFactoryRegistry
internal class PaymentMethodDescriptorFactory(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistry,
) {

    private val factories: MutableMap<String, SinglePaymentMethodDescriptorFactory> = mutableMapOf()

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

    fun register(id: String, factory: SinglePaymentMethodDescriptorFactory) {
        factories[id] = factory
    }

    fun unregister(id: SinglePaymentMethodDescriptorFactory) {
        factories.remove(id)
    }

    operator fun get(id: String): SinglePaymentMethodDescriptorFactory? = factories[id]
}

internal interface SinglePaymentMethodDescriptorFactory {

    fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor
}

