package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import org.json.JSONObject

internal sealed class BaseContinueAuthErrorDataRequest(
    open val reasonCode: ThreeDsSdkErrorReasonCode,
    open val reasonText: String,
) : JSONObjectSerializable {
    companion object {
        private const val REASON_CODE_FIELD = "reasonCode"
        private const val REASON_TEXT_FIELD = "reasonText"

        @JvmField
        val serializer =
            JSONObjectSerializer<BaseContinueAuthErrorDataRequest> { t ->
                when (t) {
                    is PreChallengeContinueAuthErrorDataRequest ->
                        PreChallengeContinueAuthErrorDataRequest.serializer.serialize(t)

                    is ChallengeRuntimeContinueAuthErrorDataRequest ->
                        ChallengeRuntimeContinueAuthErrorDataRequest.serializer.serialize(t)

                    is ChallengeProtocolContinueAuthErrorDataRequest ->
                        ChallengeProtocolContinueAuthErrorDataRequest.serializer.serialize(t)
                }
            }

        val baseSerializer =
            JSONObjectSerializer<BaseContinueAuthErrorDataRequest> { t ->
                JSONObject().apply {
                    put(REASON_CODE_FIELD, t.reasonCode.name)
                    put(REASON_TEXT_FIELD, t.reasonText)
                }
            }
    }
}
