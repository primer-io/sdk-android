package io.primer.android.domain.payments.resume.models

import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.domain.base.Params

internal data class ResumeParams(
    val id: String,
    val token: String,
    val resumeHandler: ResumeDecisionHandler
) : Params
