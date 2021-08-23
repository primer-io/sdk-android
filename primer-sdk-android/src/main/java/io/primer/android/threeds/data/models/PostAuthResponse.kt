package io.primer.android.threeds.data.models

import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.serialization.Serializable

@Serializable
internal data class PostAuthResponse(
    val token: PaymentMethodTokenInternal,
    val resumeToken: String?,
    val authentication: Authentication?,
)
