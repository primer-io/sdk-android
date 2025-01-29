package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode

internal data class PreChallengeContinueAuthErrorDataRequest(
    override val reasonCode: ThreeDsSdkErrorReasonCode,
    override val reasonText: String,
    val recoverySuggestion: String?,
) : BaseContinueAuthErrorDataRequest(
    reasonCode,
    reasonText,
) {
    companion object {
        private const val RECOVERY_SUGGESTION_FIELD = "recoverySuggestion"

        @JvmField
        val serializer =
            JSONObjectSerializer<PreChallengeContinueAuthErrorDataRequest> { t ->
                baseSerializer.serialize(t).apply {
                    putOpt(RECOVERY_SUGGESTION_FIELD, t.recoverySuggestion)
                }
            }
    }
}
