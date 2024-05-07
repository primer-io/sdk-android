package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

internal data class PaypalOrderInfo(
    val orderId: String,
    val email: String?,
    val externalPayerId: String?,
    val externalPayerFirstName: String?,
    val externalPayerLastName: String?
)
