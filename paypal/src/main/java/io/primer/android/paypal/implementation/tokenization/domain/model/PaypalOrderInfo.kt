package io.primer.android.paypal.implementation.tokenization.domain.model

internal data class PaypalOrderInfo(
    val orderId: String,
    val email: String?,
    val externalPayerId: String?,
    val externalPayerFirstName: String?,
    val externalPayerLastName: String?,
)
