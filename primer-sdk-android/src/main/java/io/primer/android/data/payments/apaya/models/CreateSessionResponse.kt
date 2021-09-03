package io.primer.android.data.payments.apaya.models

import io.primer.android.domain.payments.apaya.models.ApayaSession
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSessionResponse(
    val url: String,
    val token: String,
)

internal fun CreateSessionResponse.toApayaSession() = ApayaSession(url, token)
