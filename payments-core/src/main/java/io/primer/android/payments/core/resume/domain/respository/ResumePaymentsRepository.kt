package io.primer.android.payments.core.resume.domain.respository

import io.primer.android.payments.core.create.domain.model.PaymentResult

internal interface ResumePaymentsRepository {

    suspend fun resumePayment(paymentId: String, resumeToken: String): Result<PaymentResult>
}
