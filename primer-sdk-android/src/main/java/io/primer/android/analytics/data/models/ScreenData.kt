package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ScreenData(val height: Int, val width: Int) :
    JSONObjectSerializable, JSONDeserializable {
    companion object {

        private const val HEIGHT_FIELD = "height"
        private const val WIDTH_FIELD = "width"

        @JvmField
        val serializer = object : JSONObjectSerializer<ScreenData> {
            override fun serialize(t: ScreenData): JSONObject {
                return JSONObject().apply {
                    put(HEIGHT_FIELD, t.height)
                    put(WIDTH_FIELD, t.width)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<ScreenData> {
            override fun deserialize(t: JSONObject): ScreenData {
                return ScreenData(t.getInt(HEIGHT_FIELD), t.getInt(WIDTH_FIELD))
            }
        }
    }
}
