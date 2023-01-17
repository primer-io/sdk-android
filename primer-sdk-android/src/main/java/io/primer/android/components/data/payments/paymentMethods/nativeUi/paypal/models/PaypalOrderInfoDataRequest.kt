package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class PaypalOrderInfoDataRequest(
    val paymentMethodConfigId: String?,
    val orderId: String
) : JSONSerializable {

    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val ORDER_ID_FIELD = "orderId"

        @JvmField
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
