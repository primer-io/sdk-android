package io.primer.android.paypal.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal sealed interface PaypalConfig : PaymentMethodConfiguration {
    data class PaypalVaultConfiguration(
        val paymentMethodConfigId: String,
        val successUrl: String,
        val cancelUrl: String,
    ) : PaypalConfig

    data class PaypalCheckoutConfiguration(
        val paymentMethodConfigId: String,
        val amount: Int?,
        val currencyCode: String?,
        val successUrl: String,
        val cancelUrl: String,
    ) : PaypalConfig
}
