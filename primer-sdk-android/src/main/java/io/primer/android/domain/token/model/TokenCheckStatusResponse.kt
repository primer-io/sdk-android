package io.primer.android.domain.token.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TokenCheckStatusResponse(
    val success: Boolean? = null
)
