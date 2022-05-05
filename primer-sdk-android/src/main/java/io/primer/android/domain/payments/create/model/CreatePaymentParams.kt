package io.primer.android.domain.payments.create.model

import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.domain.base.Params

internal data class CreatePaymentParams(
    val token: String,
    val resumeHandler: ResumeDecisionHandler
) : Params
