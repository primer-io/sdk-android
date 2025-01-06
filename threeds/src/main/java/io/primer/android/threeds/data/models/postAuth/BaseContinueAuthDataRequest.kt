package io.primer.android.threeds.data.models.postAuth

import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.data.models.SdkType
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.domain.models.BaseThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import org.json.JSONObject

internal sealed class BaseContinueAuthDataRequest(
    open val status: ThreeDsAuthStatus,
    val platform: SdkType = SdkTypeResolver().resolve(),
) : JSONObjectSerializable {
    companion object {
        const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"
        private const val STATUS_FIELD = "status"
        private const val PLATFORM_FIELD = "platform"

        @JvmField
        val serializer =
            JSONObjectSerializer<BaseContinueAuthDataRequest> { t ->
                when (t) {
                    is MissingDependencyFailureContinueAuthDataRequest ->
                        MissingDependencyFailureContinueAuthDataRequest.serializer.serialize(t)

                    is DefaultFailureContinueAuthDataRequest ->
                        DefaultFailureContinueAuthDataRequest.serializer.serialize(t)

                    is SuccessContinueAuthDataRequest ->
                        SuccessContinueAuthDataRequest.serializer.serialize(t)
                }
            }

        val baseSerializer =
            JSONObjectSerializer<BaseContinueAuthDataRequest> { t ->
                JSONObject().apply {
                    put(STATUS_FIELD, t.status.name)
                    put(PLATFORM_FIELD, t.platform)
                }
            }
    }
}

internal fun BaseThreeDsContinueAuthParams.toContinueAuthDataRequest() =
    when (this) {
        is FailureThreeDsContinueAuthParams -> toContinueAuthDataRequest()
        is SuccessThreeDsContinueAuthParams -> toContinueAuthDataRequest()
    }
