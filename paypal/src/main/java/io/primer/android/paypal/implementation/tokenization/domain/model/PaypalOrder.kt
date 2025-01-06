package io.primer.android.paypal.implementation.tokenization.domain.model

internal data class PaypalOrder(
    val orderId: String,
    val approvalUrl: String,
    val successUrl: String,
    val cancelUrl: String,
)
