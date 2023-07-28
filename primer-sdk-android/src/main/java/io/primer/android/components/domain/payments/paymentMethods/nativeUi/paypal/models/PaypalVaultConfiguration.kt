package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

internal data class PaypalVaultConfiguration(
    val paymentMethodConfigId: String,
    val successUrl: String,
    val cancelUrl: String
)
