package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import org.json.JSONObject

internal data class ChallengeRuntimeContinueAuthErrorDataRequest(
    override val reasonCode: ThreeDsSdkErrorReasonCode,
    override val reasonText: String,
    val threeDsErrorCode: String?,
    val threeDsErrorDescription: String
) : BaseContinueAuthErrorDataRequest(reasonCode, reasonText) {

    companion object {

        private const val THREE_DS_ERROR_CODE_FIELD = "threeDsErrorCode"
        private const val THREE_DS_ERROR_DESCRIPTION_FIELD = "threeDsErrorDescription"

        @JvmField
        val serializer = object :
            JSONObjectSerializer<ChallengeRuntimeContinueAuthErrorDataRequest> {
            override fun serialize(t: ChallengeRuntimeContinueAuthErrorDataRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    putOpt(THREE_DS_ERROR_CODE_FIELD, t.threeDsErrorCode)
                    put(THREE_DS_ERROR_DESCRIPTION_FIELD, t.threeDsErrorDescription)
                }
            }
        }
    }
}
