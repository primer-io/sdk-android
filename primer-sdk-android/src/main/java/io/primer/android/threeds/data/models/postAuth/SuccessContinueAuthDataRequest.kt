package io.primer.android.threeds.data.models.postAuth

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import org.json.JSONObject

internal data class SuccessContinueAuthDataRequest(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String = BuildConfig.SDK_VERSION_STRING,
    val threeDsSdkProvider: ThreeDsSdkProvider = ThreeDsSdkProvider.NETCETERA
) : BaseContinueAuthDataRequest(
    ThreeDsAuthStatus.SUCCESS
) {

    companion object {

        @JvmField
        val serializer = object : JSONObjectSerializer<SuccessContinueAuthDataRequest> {

            override fun serialize(t: SuccessContinueAuthDataRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider.name)
                }
            }
        }
    }
}

internal fun SuccessThreeDsContinueAuthParams.toContinueAuthDataRequest() =
    SuccessContinueAuthDataRequest(
        threeDsSdkVersion,
        initProtocolVersion
    )
