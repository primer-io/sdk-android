package io.primer.android.domain.payments.methods.repository

import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.flow.Flow

internal interface PaymentMethodDescriptorsRepository {

    fun resolvePaymentMethodDescriptors(): Flow<List<PaymentMethodDescriptor>>

    fun getPaymentMethodDescriptors(): List<PaymentMethodDescriptor>
}
