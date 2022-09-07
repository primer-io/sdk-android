package io.primer.android.data.payments.paypal.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import org.json.JSONObject

internal data class PaypalOrderInfoDataRequest(
    val paymentMethodConfigId: String,
    val orderId: String
) : JSONSerializable {

    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val ORDER_ID_FIELD = "orderId"

        val serializer = object : JSONSerializer<PaypalOrderInfoDataRequest> {
            override fun serialize(t: PaypalOrderInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(ORDER_ID_FIELD, t.orderId)
                }
            }
        }
    }
}

internal fun PaypalOrderInfoParams.toPaypalOrderInfoRequest() = PaypalOrderInfoDataRequest(
    paymentMethodConfigId,
    orderId
)
