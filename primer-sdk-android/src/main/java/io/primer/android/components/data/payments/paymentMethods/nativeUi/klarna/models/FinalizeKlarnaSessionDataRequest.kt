package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class FinalizeKlarnaSessionDataRequest(
    private val paymentMethodConfigId: String,
    private val sessionId: String
) : JSONObjectSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_ID_FIELD = "sessionId"

        @JvmField
        val serializer = object : JSONObjectSerializer<FinalizeKlarnaSessionDataRequest> {
            override fun serialize(t: FinalizeKlarnaSessionDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(SESSION_ID_FIELD, t.sessionId)
                }
            }
        }
    }
}
