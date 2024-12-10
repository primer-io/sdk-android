package io.primer.android.components.implementation.domain

import io.primer.android.paymentmethods.PaymentMethodDescriptor

internal interface PaymentMethodDescriptorsRepository {

    suspend fun resolvePaymentMethodDescriptors(): Result<List<PaymentMethodDescriptor>>

    fun getPaymentMethodDescriptors(): List<PaymentMethodDescriptor>
}
