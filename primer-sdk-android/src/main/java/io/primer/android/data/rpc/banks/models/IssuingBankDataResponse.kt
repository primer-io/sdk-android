package io.primer.android.data.rpc.banks.models

import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse.Companion.DISABLED_FIELD
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse.Companion.ICON_URL_FIELD
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse.Companion.ID_FIELD
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse.Companion.NAME_FIELD
import org.json.JSONObject

internal data class IssuingBankResultDataResponse(
    val result: List<IssuingBankDataResponse>
) : JSONDeserializable {

    companion object {
        private const val RESULT_FIELD = "result"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                nonPrimitiveKey(RESULT_FIELD) {
                    primitiveKey(ID_FIELD)
                    primitiveKey(NAME_FIELD)
                    primitiveKey(DISABLED_FIELD)
                    primitiveKey(ICON_URL_FIELD)
                }
            }
        }

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
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val DISABLED_FIELD = "disabled"
        const val ICON_URL_FIELD = "iconUrl"

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
