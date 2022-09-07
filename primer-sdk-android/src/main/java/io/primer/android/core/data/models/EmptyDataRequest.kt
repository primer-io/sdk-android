package io.primer.android.core.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal class EmptyDataRequest : JSONSerializable, JSONDeserializable {

    companion object {
        @JvmField
        val serializer = object : JSONSerializer<EmptyDataRequest> {
            override fun serialize(t: EmptyDataRequest): JSONObject {
                return JSONObject()
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<EmptyDataRequest> {
            override fun deserialize(t: JSONObject): EmptyDataRequest {
                return EmptyDataRequest()
            }
        }
    }
}
