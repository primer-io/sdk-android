package io.primer.checkout.orchestrator.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.statetransport.domain.model.CurrentAttempt
import org.json.JSONObject

/**
 * The SDK-owned portion of the initial state handed to the state processor. These keys are
 * merged over the schema-provided parameters and can never be shadowed by them.
 */
internal data class SdkOwnedInitialState(
    val sdk: SdkUrls,
    val currentAttempt: CurrentAttempt?,
) : JSONObjectSerializable {

    internal data class SdkUrls(
        val pciUrl: String,
        val coreUrl: String,
    )

    companion object {
        private const val SDK_FIELD = "sdk"
        private const val PCI_URL_FIELD = "pciUrl"
        private const val CORE_URL_FIELD = "coreUrl"
        private const val CURRENT_ATTEMPT_FIELD = "currentAttempt"

        /**
         * Every key the SDK owns in the initial state. Callers remove them from the schema
         * parameters before applying the serialized overlay, so an absent envelope value still
         * deletes a colliding schema-provided key.
         */
        val SDK_OWNED_KEYS = listOf(SDK_FIELD, CURRENT_ATTEMPT_FIELD)

        @JvmField
        val serializer = JSONObjectSerializer<SdkOwnedInitialState> { t ->
            JSONObject().apply {
                put(
                    SDK_FIELD,
                    JSONObject().apply {
                        put(PCI_URL_FIELD, t.sdk.pciUrl)
                        put(CORE_URL_FIELD, t.sdk.coreUrl)
                    },
                )
                putOpt(
                    CURRENT_ATTEMPT_FIELD,
                    t.currentAttempt?.let {
                        JSONSerializationUtils.getJsonObjectSerializer<CurrentAttempt>().serialize(it)
                    },
                )
            }
        }
    }
}
