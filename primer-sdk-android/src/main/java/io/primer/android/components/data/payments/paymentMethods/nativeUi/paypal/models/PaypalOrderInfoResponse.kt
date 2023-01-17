package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfo
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
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
    val externalPayerId: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?
) : JSONSerializable, JSONDeserializable {
    companion object {
        private const val EXTERNAL_PAYER_ID_FIELD = "externalPayerId"
        private const val EMAIL_FIELD = "email"
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"

        @JvmField
        val serializer = object : JSONSerializer<PaypalExternalPayerInfo> {

            override fun serialize(t: PaypalExternalPayerInfo): JSONObject {
                return JSONObject().apply {
                    putOpt(EXTERNAL_PAYER_ID_FIELD, t.externalPayerId)
                    putOpt(EMAIL_FIELD, t.email)
                    putOpt(FIRST_NAME_FIELD, t.firstName)
                    putOpt(LAST_NAME_FIELD, t.lastName)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<PaypalExternalPayerInfo> {

            override fun deserialize(t: JSONObject): PaypalExternalPayerInfo {
                return PaypalExternalPayerInfo(
                    t.optNullableString(EXTERNAL_PAYER_ID_FIELD),
                    t.optNullableString(EMAIL_FIELD),
                    t.optNullableString(FIRST_NAME_FIELD),
                    t.optNullableString(LAST_NAME_FIELD)
                )
            }
        }
    }
}

internal fun PaypalOrderInfoResponse.toPaypalOrder() = PaypalOrderInfo(
    orderId,
    externalPayerInfo?.email
)
