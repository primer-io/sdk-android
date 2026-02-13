package io.primer.android.payments.core.create.domain.repository

import io.primer.android.payments.core.create.domain.model.CreatePaymentParams
import io.primer.android.payments.core.create.domain.model.PaymentResult

internal interface CreatePaymentRepository {
    suspend fun createPayment(params: CreatePaymentParams): Result<PaymentResult>
}
