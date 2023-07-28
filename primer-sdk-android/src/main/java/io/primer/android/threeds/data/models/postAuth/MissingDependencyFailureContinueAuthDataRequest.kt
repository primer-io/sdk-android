package io.primer.android.threeds.data.models.postAuth

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest
import org.json.JSONObject

internal data class MissingDependencyFailureContinueAuthDataRequest(
    override val error: PreChallengeContinueAuthErrorDataRequest
) : BaseFailureContinueAuthDataRequest(error) {

    companion object {
        @JvmField
        val serializer =
            object : JSONObjectSerializer<MissingDependencyFailureContinueAuthDataRequest> {
                override fun serialize(t: MissingDependencyFailureContinueAuthDataRequest):
                    JSONObject {
                    return baseErrorSerializer.serialize(t)
                }
            }
    }
}
