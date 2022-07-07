package io.primer.android.payment.processor3ds

import io.primer.android.data.configuration.models.PaymentMethodType

internal data class Processor3DS(
    val redirectUrl: String,
    val statusUrl: String,
    val title: String = "3D Secure",
    val paymentMethodType: PaymentMethodType = PaymentMethodType.PAYMENT_CARD
)
