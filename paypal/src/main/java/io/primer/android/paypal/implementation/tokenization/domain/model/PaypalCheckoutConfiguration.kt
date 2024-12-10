package io.primer.android.paypal.implementation.tokenization.domain.model

internal data class PaypalCheckoutConfiguration(
    val paymentMethodConfigId: String,
    val amount: Int,
    val currencyCode: String?,
    val successUrl: String,
    val cancelUrl: String
)
