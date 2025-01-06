package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.core.domain.Params

internal data class PaypalOrderInfoParams(
    val paymentMethodConfigId: String?,
    val orderId: String?,
) : Params
