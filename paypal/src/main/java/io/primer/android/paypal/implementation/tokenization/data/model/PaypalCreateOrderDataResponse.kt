package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder

internal data class PaypalCreateOrderDataResponse(
    val orderId: String,
    val approvalUrl: String,
) : JSONDeserializable {
    companion object {
        private const val ORDER_ID_FIELD = "orderId"
        private const val APPROVAL_URL_FIELD = "approvalUrl"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaypalCreateOrderDataResponse(
                    t.getString(ORDER_ID_FIELD),
                    t.getString(APPROVAL_URL_FIELD),
                )
            }
    }
}

internal fun PaypalCreateOrderDataResponse.toPaypalOrder(
    successUrl: String,
    cancelUrl: String,
) = PaypalOrder(
    orderId,
    approvalUrl,
    successUrl,
    cancelUrl,
)
