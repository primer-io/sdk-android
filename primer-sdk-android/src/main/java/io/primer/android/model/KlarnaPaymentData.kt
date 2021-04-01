package io.primer.android.model

data class KlarnaPaymentData(
    val redirectUrl: String,
    val returnUrl: String,
    val sessionId: String,
)
