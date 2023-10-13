package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import org.json.JSONObject

internal data class ChallengeProtocolContinueAuthErrorDataRequest(
    override val reasonCode: ThreeDsSdkErrorReasonCode,
    override val reasonText: String,
    val threeDsErrorCode: String?,
    val threeDsErrorDescription: String,
    val threeDsErrorComponent: String,
    val threeDsErrorDetails: String,
    val threeDsSdkTransactionId: String,
    val protocolVersion: String
) : BaseContinueAuthErrorDataRequest(reasonCode, reasonText) {

    companion object {

        private const val THREE_DS_ERROR_CODE_FIELD = "threeDsErrorCode"
        private const val THREE_DS_ERROR_DESCRIPTION_FIELD = "threeDsErrorDescription"
        private const val THREE_DS_ERROR_COMPONENT_FIELD = "threeDsErrorComponent"
        private const val THREE_DS_ERROR_DETAIL_FIELD = "threeDsErrorDetails"
        private const val THREE_DS_SDK_TRANSACTION_ID_FIELD = "threeDsSdkTransactionId"
        private const val THREE_DS_PROTOCOL_VERSION_FIELD = "protocolVersion"

        @JvmField
        val serializer = object :
            JSONObjectSerializer<ChallengeProtocolContinueAuthErrorDataRequest> {
            override fun serialize(t: ChallengeProtocolContinueAuthErrorDataRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    putOpt(THREE_DS_ERROR_CODE_FIELD, t.threeDsErrorCode)
                    put(THREE_DS_ERROR_DESCRIPTION_FIELD, t.threeDsErrorDescription)
                    put(THREE_DS_ERROR_COMPONENT_FIELD, t.threeDsErrorComponent)
                    put(THREE_DS_ERROR_DETAIL_FIELD, t.threeDsErrorDetails)
                    put(THREE_DS_SDK_TRANSACTION_ID_FIELD, t.threeDsSdkTransactionId)
                    put(THREE_DS_PROTOCOL_VERSION_FIELD, t.protocolVersion)
                }
            }
        }
    }
}
