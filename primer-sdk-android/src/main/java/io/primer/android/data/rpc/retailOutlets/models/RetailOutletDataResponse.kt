package io.primer.android.data.rpc.retailOutlets.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class RetailOutletResultDataResponse(
    val result: List<RetailOutletDataResponse>
) : JSONDeserializable {

    companion object {
        private const val RESULT_FIELD = "result"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            RetailOutletResultDataResponse(
                t.optJSONArray(RESULT_FIELD)?.sequence<JSONObject>()?.map {
                    JSONSerializationUtils
                        .getJsonObjectDeserializer<RetailOutletDataResponse>()
                        .deserialize(it)
                }?.toList().orEmpty()
            )
        }
    }
}

internal data class RetailOutletDataResponse(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String
) : JSONDeserializable {
    companion object {
        private const val ID_FIELD = "id"
        private const val NAME_FIELD = "name"
        private const val DISABLED_FIELD = "disabled"
        private const val ICON_URL_FIELD = "iconUrl"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            RetailOutletDataResponse(
                t.getString(ID_FIELD),
                t.getString(NAME_FIELD),
                t.getBoolean(DISABLED_FIELD),
                t.getString(ICON_URL_FIELD)
            )
        }
    }
}
