package io.primer.android.domain.payments.paypal.models

internal data class PaypalOrderInfo(
    val orderId: String,
    val email: String?
)
