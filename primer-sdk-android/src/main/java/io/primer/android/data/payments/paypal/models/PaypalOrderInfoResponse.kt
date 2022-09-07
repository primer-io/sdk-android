package io.primer.android.data.payments.paypal.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfo
import org.json.JSONObject

internal data class PaypalOrderInfoResponse(
    val orderId: String,
    val externalPayerInfo: PaypalExternalPayerInfo?
) : JSONDeserializable {
    companion object {
        private const val ORDER_ID_FIELD = "orderId"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"

        @JvmField
        val deserializer = object : JSONDeserializer<PaypalOrderInfoResponse> {

            override fun deserialize(t: JSONObject): PaypalOrderInfoResponse {
                return PaypalOrderInfoResponse(
                    t.getString(ORDER_ID_FIELD),
                    t.optJSONObject(EXTERNAL_PAYER_INFO_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<PaypalExternalPayerInfo>()
                            .deserialize(it)
                    }
                )
            }
        }
    }
}

internal data class PaypalExternalPayerInfo(
    val email: String?
) : JSONDeserializable {
    companion object {
        private const val EMAIL_FIELD = "email"

        @JvmField
        val deserializer = object : JSONDeserializer<PaypalExternalPayerInfo> {

            override fun deserialize(t: JSONObject): PaypalExternalPayerInfo {
                return PaypalExternalPayerInfo(
                    t.optNullableString(EMAIL_FIELD)
                )
            }
        }
    }
}

internal fun PaypalOrderInfoResponse.toPaypalOrder() = PaypalOrderInfo(
    orderId,
    externalPayerInfo?.email
)
