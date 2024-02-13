package io.primer.android.threeds.data.models.postAuth

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest

internal data class MissingDependencyFailureContinueAuthDataRequest(
    override val error: PreChallengeContinueAuthErrorDataRequest
) : BaseFailureContinueAuthDataRequest(error) {

    companion object {
        @JvmField
        val serializer =
            JSONObjectSerializer<MissingDependencyFailureContinueAuthDataRequest> { t ->
                baseErrorSerializer.serialize(t)
            }
    }
}
