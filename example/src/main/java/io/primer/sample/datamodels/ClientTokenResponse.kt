package io.primer.sample.datamodels

import androidx.annotation.Keep

@Keep
data class ClientTokenResponse(
    val clientToken: String,
    val expirationDate: String,
)
