package io.primer.android.threeds.data.models.postAuth

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.models.postAuth.error.BaseContinueAuthErrorDataRequest
import org.json.JSONObject

internal class DefaultFailureContinueAuthDataRequest(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String?,
    override val error: BaseContinueAuthErrorDataRequest,
    val threeDsWrapperSdkVersion: String = BuildConfig.SDK_VERSION_STRING,
    val threeDsSdkProvider: ThreeDsSdkProvider = ThreeDsSdkProvider.NETCETERA
) : BaseFailureContinueAuthDataRequest(error) {

    companion object {

        @JvmField
        val serializer = object : JSONObjectSerializer<DefaultFailureContinueAuthDataRequest> {

            override fun serialize(t: DefaultFailureContinueAuthDataRequest): JSONObject {
                return baseErrorSerializer.serialize(t).apply {
                    putOpt(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider.name)
                }
            }
        }
    }
}
