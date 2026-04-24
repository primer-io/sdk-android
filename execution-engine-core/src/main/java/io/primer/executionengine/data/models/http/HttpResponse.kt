package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.toMap

internal data class HttpResponse(val data: Map<String, Any?>) : JSONDeserializable {
    companion object {
        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                HttpResponse(t.toMap())
            }
    }
}
