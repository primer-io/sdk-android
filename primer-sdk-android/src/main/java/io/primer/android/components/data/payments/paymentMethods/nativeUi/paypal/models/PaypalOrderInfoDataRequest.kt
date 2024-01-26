package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class PaypalOrderInfoDataRequest(
    val paymentMethodConfigId: String?,
    val orderId: String
) : JSONObjectSerializable {

    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val ORDER_ID_FIELD = "orderId"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
            }
        }

        @JvmField
        val serializer = object : JSONObjectSerializer<PaypalOrderInfoDataRequest> {
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
