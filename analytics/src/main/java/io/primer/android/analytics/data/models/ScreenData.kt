package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ScreenData(val height: Int, val width: Int) :
    JSONObjectSerializable, JSONDeserializable {
    companion object {

        private const val HEIGHT_FIELD = "height"
        private const val WIDTH_FIELD = "width"

        @JvmField
        val serializer = JSONObjectSerializer<ScreenData> { t ->
            JSONObject().apply {
                put(HEIGHT_FIELD, t.height)
                put(WIDTH_FIELD, t.width)
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            ScreenData(
                t.getInt(HEIGHT_FIELD),
                t.getInt(WIDTH_FIELD)
            )
        }
    }
}
