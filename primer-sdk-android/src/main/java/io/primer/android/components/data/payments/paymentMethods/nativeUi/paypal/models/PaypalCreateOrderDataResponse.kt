package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrder
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import org.json.JSONObject

internal data class PaypalCreateOrderDataResponse(
    val orderId: String,
    val approvalUrl: String
) : JSONDeserializable {

    companion object {

        private const val ORDER_ID_FIELD = "orderId"
        private const val APPROVAL_URL_FIELD = "approvalUrl"

        @JvmField
        val deserializer = object : JSONDeserializer<PaypalCreateOrderDataResponse> {

            override fun deserialize(t: JSONObject): PaypalCreateOrderDataResponse {
                return PaypalCreateOrderDataResponse(
                    t.getString(ORDER_ID_FIELD),
                    t.getString(APPROVAL_URL_FIELD)
                )
            }
        }
    }
}

internal fun PaypalCreateOrderDataResponse.toPaypalOrder(
    successUrl: String,
    cancelUrl: String
) = PaypalOrder(
    orderId,
    approvalUrl,
    successUrl,
    cancelUrl
)
