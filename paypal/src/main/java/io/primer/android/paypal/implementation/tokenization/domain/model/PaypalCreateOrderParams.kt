package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.core.domain.Params

internal data class PaypalCreateOrderParams(
    val paymentMethodConfigId: String,
    val amount: Int?,
    val currencyCode: String?,
    val successUrl: String,
    val cancelUrl: String,
) : Params
