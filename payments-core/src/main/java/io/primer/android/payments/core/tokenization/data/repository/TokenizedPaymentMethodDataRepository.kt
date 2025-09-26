package io.primer.android.payments.core.tokenization.data.repository

import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal class TokenizedPaymentMethodDataRepository : TokenizedPaymentMethodRepository {
    private var paymentMethodTokenInternal: PaymentMethodTokenInternal? = null

    override fun getPaymentMethod() = requireNotNull(paymentMethodTokenInternal)

    override fun setPaymentMethod(paymentMethodTokenInternal: PaymentMethodTokenInternal?) {
        this.paymentMethodTokenInternal = paymentMethodTokenInternal
    }
}
