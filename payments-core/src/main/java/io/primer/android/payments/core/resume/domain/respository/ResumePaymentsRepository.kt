package io.primer.android.payments.core.resume.domain.respository

import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.resume.domain.models.ResumeParams

internal interface ResumePaymentsRepository {
    suspend fun resumePayment(params: ResumeParams): Result<PaymentResult>
}
