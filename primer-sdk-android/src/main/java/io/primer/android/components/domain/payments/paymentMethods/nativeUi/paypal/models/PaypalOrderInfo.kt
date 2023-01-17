package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

internal data class PaypalOrderInfo(
    val orderId: String,
    val email: String?
)
