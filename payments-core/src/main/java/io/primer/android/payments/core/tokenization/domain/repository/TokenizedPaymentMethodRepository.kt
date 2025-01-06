package io.primer.android.payments.core.tokenization.domain.repository

import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal

interface TokenizedPaymentMethodRepository {
    fun getPaymentMethod(): PaymentMethodTokenInternal

    fun setPaymentMethod(paymentMethodTokenInternal: PaymentMethodTokenInternal)
}
