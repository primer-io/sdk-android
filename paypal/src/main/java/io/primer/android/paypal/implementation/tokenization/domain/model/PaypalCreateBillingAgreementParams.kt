package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.core.domain.Params

internal data class PaypalCreateBillingAgreementParams(
    val paymentMethodConfigId: String,
    val successUrl: String,
    val cancelUrl: String
) : Params
