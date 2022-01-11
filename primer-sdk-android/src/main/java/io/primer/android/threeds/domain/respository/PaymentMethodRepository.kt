package io.primer.android.threeds.domain.respository

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal

internal interface PaymentMethodRepository {

    fun getPaymentMethod(): PaymentMethodTokenInternal

    fun setPaymentMethod(paymentMethodTokenInternal: PaymentMethodTokenInternal)
}
