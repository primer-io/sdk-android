package io.primer.android.domain.payments.paypal.models

import io.primer.android.domain.base.Params

internal data class PaypalOrderInfoParams(
    val paymentMethodConfigId: String,
    val orderId: String
) : Params
