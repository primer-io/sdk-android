package io.primer.android.threeds.data.repository

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.threeds.domain.respository.PaymentMethodRepository

internal class PaymentMethodDataRepository : PaymentMethodRepository {

    private lateinit var paymentMethodTokenInternal: PaymentMethodTokenInternal

    override fun getPaymentMethod() = paymentMethodTokenInternal

    override fun setPaymentMethod(paymentMethodTokenInternal: PaymentMethodTokenInternal) {
        this.paymentMethodTokenInternal = paymentMethodTokenInternal
    }
}
