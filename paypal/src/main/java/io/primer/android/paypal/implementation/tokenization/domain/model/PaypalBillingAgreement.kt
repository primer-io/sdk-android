package io.primer.android.paypal.implementation.tokenization.domain.model

internal data class PaypalBillingAgreement(
    val paymentMethodConfigId: String,
    val approvalUrl: String,
    val successUrl: String,
    val cancelUrl: String,
)
