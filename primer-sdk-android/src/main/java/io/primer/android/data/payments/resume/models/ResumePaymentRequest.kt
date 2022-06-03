package io.primer.android.data.payments.resume.models

import kotlinx.serialization.Serializable

@Serializable
internal data class ResumePaymentRequest(val resumeToken: String)
