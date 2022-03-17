package io.primer.android.domain.token.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class TokenCheckStatusResponse(
    val success: Boolean? = null
)
