package io.primer.android.data.payments.paypal.models

import io.primer.android.domain.payments.paypal.models.PaypalOrderInfo
import kotlinx.serialization.Serializable

@Serializable
internal data class PaypalOrderInfoResponse(
    val orderId: String,
    val externalPayerInfo: PaypalExternalPayerInfo? = null
)

@Serializable
internal data class PaypalExternalPayerInfo(
    val email: String? = null
)

internal fun PaypalOrderInfoResponse.toPaypalOrder() = PaypalOrderInfo(
    orderId,
    externalPayerInfo?.email
)
