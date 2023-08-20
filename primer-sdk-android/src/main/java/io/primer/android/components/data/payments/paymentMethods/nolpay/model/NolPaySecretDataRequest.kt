package io.primer.android.components.data.payments.paymentMethods.nolpay.model

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class NolPaySecretDataRequest(val sdkId: String) : JSONObjectSerializable {

    companion object {

        private const val SDK_ID_FIELD = "sdkId"

        @JvmField
        val serializer = object : JSONObjectSerializer<NolPaySecretDataRequest> {
            override fun serialize(t: NolPaySecretDataRequest): JSONObject {
                return JSONObject().apply {
                    put(SDK_ID_FIELD, t.sdkId)
                }
            }
        }
    }
}
