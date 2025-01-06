package io.primer.android.payments.core.resume.domain.handler

import io.primer.android.payments.core.create.domain.model.PaymentDecision

fun interface PostResumeHandler {
    suspend fun handle(resumeToken: String): Result<PaymentDecision>
}
