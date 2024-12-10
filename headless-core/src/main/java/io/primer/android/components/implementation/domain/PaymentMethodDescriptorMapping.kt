package io.primer.android.components.implementation.domain

import io.primer.android.paymentmethods.PaymentMethodDescriptor

internal class PaymentMethodDescriptorMapping(
    private val descriptors: List<PaymentMethodDescriptor>
) {

    fun getDescriptorFor(paymentMethod: String): PaymentMethodDescriptor? =
        descriptors.find { it.config.type == paymentMethod }
}
