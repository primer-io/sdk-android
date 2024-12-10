package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import org.json.JSONObject

internal data class PaypalOrderInfoDataRequest(
    val paymentMethodConfigId: String?,
    val orderId: String
) : JSONObjectSerializable {

    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val ORDER_ID_FIELD = "orderId"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> =
                whitelistedKeys {
                    primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                }
        }

        @JvmField
        val serializer = JSONObjectSerializer<PaypalOrderInfoDataRequest> { t ->
            JSONObject().apply {
                put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                put(ORDER_ID_FIELD, t.orderId)
            }
        }
    }
}

internal fun PaypalOrderInfoParams.toPaypalOrderInfoRequest() = PaypalOrderInfoDataRequest(
    paymentMethodConfigId,
    requireNotNull(orderId)
)
