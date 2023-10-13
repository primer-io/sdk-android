package io.primer.android.data.rpc.banks.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class IssuingBankResultDataResponse(
    val result: List<IssuingBankDataResponse>
) : JSONDeserializable {

    companion object {
        private const val RESULT_FIELD = "result"

        @JvmField
        val deserializer = object : JSONDeserializer<IssuingBankResultDataResponse> {

            override fun deserialize(t: JSONObject): IssuingBankResultDataResponse {
                return IssuingBankResultDataResponse(
                    t.optJSONArray(RESULT_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils
                            .getDeserializer<IssuingBankDataResponse>()
                            .deserialize(it)
                    }?.toList().orEmpty()
                )
            }
        }
    }
}

internal data class IssuingBankDataResponse(
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
        val deserializer = object : JSONDeserializer<IssuingBankDataResponse> {

            override fun deserialize(t: JSONObject): IssuingBankDataResponse {
                return IssuingBankDataResponse(
                    t.getString(ID_FIELD),
                    t.getString(NAME_FIELD),
                    t.getBoolean(DISABLED_FIELD),
                    t.getString(ICON_URL_FIELD)
                )
            }
        }
    }
}
