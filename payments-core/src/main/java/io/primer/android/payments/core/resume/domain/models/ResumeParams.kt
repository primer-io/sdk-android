package io.primer.android.payments.core.resume.domain.models

import io.primer.android.core.domain.Params

data class ResumeParams(
    val paymentId: String,
    val resumeToken: String
) : Params
