package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

internal data class NolPayRequiredAction(
    val transactionNumber: String,
    val statusUrl: String,
    val completeUrl: String,
    val paymentMethodType: String
)
