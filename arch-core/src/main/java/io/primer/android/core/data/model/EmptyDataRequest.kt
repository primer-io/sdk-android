package io.primer.android.core.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

class EmptyDataRequest : JSONObjectSerializable {

    companion object {
        @JvmField
        val serializer = JSONObjectSerializer<EmptyDataRequest> { JSONObject() }
    }
}
