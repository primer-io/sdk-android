package io.primer.android.data.payments.paypal.models

import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import kotlinx.serialization.Serializable

@Serializable
internal data class PaypalOrderInfoRequest(
    val paymentMethodConfigId: String,
    val orderId: String
)

internal fun PaypalOrderInfoParams.toPaypalOrderInfoRequest() = PaypalOrderInfoRequest(
    paymentMethodConfigId,
    orderId
)
