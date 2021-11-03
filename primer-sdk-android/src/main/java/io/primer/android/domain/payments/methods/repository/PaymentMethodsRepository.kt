package io.primer.android.domain.payments.methods.repository

import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.flow.Flow

internal interface PaymentMethodsRepository {

    fun getPaymentMethodDescriptors(): Flow<List<PaymentMethodDescriptor>>
}
