package io.primer.android.payments.core.create.domain.repository

import io.primer.android.payments.core.create.domain.model.PaymentResult

interface PaymentResultRepository {

    fun getPaymentResult(): PaymentResult
}
