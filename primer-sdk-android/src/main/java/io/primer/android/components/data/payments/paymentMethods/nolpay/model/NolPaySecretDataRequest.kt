package io.primer.android.components.data.payments.paymentMethods.nolpay.model

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class NolPaySecretDataRequest(
    val sdkId: String,
    val appId: String,
    val deviceVendor: String,
    val deviceModel: String
) : JSONObjectSerializable {

    companion object {

        private const val SDK_ID_FIELD = "nolSdkId"
        private const val APP_ID_FIELD = "nolAppId"
        private const val PHONE_VENDOR_FIELD = "phoneVendor"
        private const val PHONE_MODEL_FIELD = "phoneModel"

        @JvmField
        val serializer = object : JSONObjectSerializer<NolPaySecretDataRequest> {
            override fun serialize(t: NolPaySecretDataRequest): JSONObject {
                return JSONObject().apply {
                    put(SDK_ID_FIELD, t.sdkId)
                    put(APP_ID_FIELD, t.appId)
                    put(PHONE_VENDOR_FIELD, t.deviceVendor)
                    put(PHONE_MODEL_FIELD, t.deviceModel)
                }
            }
        }
    }
}
