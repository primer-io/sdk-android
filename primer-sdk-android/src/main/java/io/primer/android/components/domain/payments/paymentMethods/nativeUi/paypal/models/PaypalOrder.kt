package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

internal data class PaypalOrder(
    val orderId: String,
    val approvalUrl: String,
    val successUrl: String,
    val cancelUrl: String
)
