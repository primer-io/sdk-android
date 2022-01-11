package io.primer.android.threeds.data.models

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import kotlinx.serialization.Serializable

@Serializable
internal data class BeginAuthResponse(
    val token: PaymentMethodTokenInternal,
    val authentication: Authentication,
    val resumeToken: String,
)
