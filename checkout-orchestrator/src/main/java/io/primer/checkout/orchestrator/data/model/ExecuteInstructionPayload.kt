package io.primer.checkout.orchestrator.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

internal data class ExecuteInstructionPayload(
    val schema: String,
    val parameters: String,
) : JSONDeserializable {

    companion object {
        private const val SCHEMA_FIELD = "schema"
        private const val PARAMETERS_FIELD = "parameters"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            ExecuteInstructionPayload(
                schema = json.getJSONObject(SCHEMA_FIELD).toString(),
                parameters = json.getJSONObject(PARAMETERS_FIELD).toString(),
            )
        }
    }
}
