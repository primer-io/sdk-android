package io.primer.android.threeds.data.models.auth

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class SDKAuthDataRequest(
    val sdkAppId: String,
    val sdkTransactionId: String,
    val sdkTimeout: Int,
    val sdkEncData: String,
    val sdkEphemPubKey: String,
    val sdkReferenceNumber: String,
) : JSONObjectSerializable {

    companion object {
        private const val SDK_APP_ID_FIELD = "sdkAppId"
        private const val SDK_TRANSACTION_ID_FIELD = "sdkTransactionId"
        private const val SDK_TIMEOUT_FIELD = "sdkTimeout"
        private const val SDK_ENC_DATA_FIELD = "sdkEncData"
        private const val SDK_EPHEM_PUB_KEY_FIELD = "sdkEphemPubKey"
        private const val SDK_REFERENCE_NUMBER_FIELD = "sdkReferenceNumber"

        @JvmField
        val serializer = object : JSONObjectSerializer<SDKAuthDataRequest> {
            override fun serialize(t: SDKAuthDataRequest): JSONObject {
                return JSONObject().apply {
                    put(SDK_APP_ID_FIELD, t.sdkAppId)
                    put(SDK_TRANSACTION_ID_FIELD, t.sdkTransactionId)
                    put(SDK_TIMEOUT_FIELD, t.sdkTimeout)
                    put(SDK_ENC_DATA_FIELD, t.sdkEncData)
                    put(SDK_EPHEM_PUB_KEY_FIELD, t.sdkEphemPubKey)
                    put(SDK_REFERENCE_NUMBER_FIELD, t.sdkReferenceNumber)
                }
            }
        }
    }
}
