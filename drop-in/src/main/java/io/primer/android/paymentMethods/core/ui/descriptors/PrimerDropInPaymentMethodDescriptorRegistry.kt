package io.primer.android.paymentMethods.core.ui.descriptors

internal class PrimerDropInPaymentMethodDescriptorRegistry {
    private val factories: MutableMap<String, PaymentMethodDropInDescriptor> =
        mutableMapOf()

    fun register(
        type: String,
        descriptor: PaymentMethodDropInDescriptor,
    ) {
        factories[type] = descriptor
    }

    fun unregister(type: String) {
        factories.remove(type)
    }

    operator fun get(type: String): PaymentMethodDropInDescriptor? = factories[type]
}
