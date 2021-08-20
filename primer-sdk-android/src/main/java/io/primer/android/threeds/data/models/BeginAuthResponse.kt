package io.primer.android.threeds.data.models

import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.serialization.Serializable

@Serializable
internal data class BeginAuthResponse(
    val token: PaymentMethodTokenInternal,
    val authentication: Authentication,
    val resumeToken: String,
)
