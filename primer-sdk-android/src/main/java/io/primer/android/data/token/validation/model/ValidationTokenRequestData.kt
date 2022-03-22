package io.primer.android.data.token.validation.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ValidationTokenRequestData(
    val clientToken: String
)

internal fun String.toValidationTokenData() = ValidationTokenRequestData(this)
