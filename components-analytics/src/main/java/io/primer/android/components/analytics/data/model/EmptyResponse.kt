package io.primer.android.components.analytics.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

/**
 * Empty response for endpoints that don't return data.
 */
internal data class EmptyResponse(val placeholder: String = "") : JSONDeserializable {
    companion object {
        @JvmField
        val deserializer = JSONObjectDeserializer<EmptyResponse> { _: JSONObject -> EmptyResponse() }
    }
}
