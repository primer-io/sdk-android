package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

internal data class PaypalCheckoutConfiguration(
    val paymentMethodConfigId: String,
    val amount: Int,
    val currencyCode: String?,
    val successUrl: String,
    val cancelUrl: String
)
