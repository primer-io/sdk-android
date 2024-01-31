package io.primer.android.core.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal class EmptyDataRequest : JSONObjectSerializable, JSONDeserializable {

    companion object {
        @JvmField
        val serializer = object : JSONObjectSerializer<EmptyDataRequest> {
            override fun serialize(t: EmptyDataRequest): JSONObject {
                return JSONObject()
            }
        }

        @JvmField
        val deserializer = object : JSONObjectDeserializer<EmptyDataRequest> {
            override fun deserialize(t: JSONObject): EmptyDataRequest {
                return EmptyDataRequest()
            }
        }
    }
}
