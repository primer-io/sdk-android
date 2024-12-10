package io.primer.android.sandboxProcessor.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class SandboxProcessorSessionInfoDataRequest(
    val flowDecision: String,
    val platform: String = "ANDROID"
) : JSONObjectSerializable {

    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val FLOW_DECISION_FIELD = "flowDecision"

        @JvmField
        val serializer = JSONObjectSerializer<SandboxProcessorSessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(PLATFORM_FIELD, t.platform)
                put(FLOW_DECISION_FIELD, t.flowDecision)
            }
        }
    }
}
