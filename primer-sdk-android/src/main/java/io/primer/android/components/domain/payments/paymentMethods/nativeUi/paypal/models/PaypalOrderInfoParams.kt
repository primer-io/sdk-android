package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.domain.base.Params

internal data class PaypalOrderInfoParams(
    val paymentMethodConfigId: String?,
    val orderId: String
) : Params
