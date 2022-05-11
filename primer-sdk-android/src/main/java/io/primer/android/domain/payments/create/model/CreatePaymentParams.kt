package io.primer.android.domain.payments.create.model

import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.domain.base.Params

internal data class CreatePaymentParams(
    val token: String,
    val resumeHandler: PrimerResumeDecisionHandler
) : Params
