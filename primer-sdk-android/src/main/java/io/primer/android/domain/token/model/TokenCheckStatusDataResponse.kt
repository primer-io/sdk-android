package io.primer.android.domain.token.model

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

internal data class TokenCheckStatusDataResponse(
    val success: Boolean?
) : JSONDeserializable {
    companion object {
        private const val SUCCESS_FIELD = "success"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<TokenCheckStatusDataResponse> {

            override fun deserialize(t: JSONObject): TokenCheckStatusDataResponse {
                return TokenCheckStatusDataResponse(t.optBoolean(SUCCESS_FIELD))
            }
        }
    }
}
