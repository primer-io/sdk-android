package io.primer.android.domain.payments.resume.respository

import io.primer.android.domain.payments.create.model.PaymentResult
import kotlinx.coroutines.flow.Flow

internal interface ResumePaymentsRepository {

    fun resumePayment(id: String, token: String): Flow<PaymentResult>
}
