package io.primer.android.threeds.data.models

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import kotlinx.serialization.Serializable

@Serializable
internal data class PostAuthResponse(
    val token: PaymentMethodTokenInternal,
    val resumeToken: String?,
    val authentication: Authentication?,
)
