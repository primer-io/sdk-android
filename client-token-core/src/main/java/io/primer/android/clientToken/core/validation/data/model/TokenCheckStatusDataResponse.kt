package io.primer.android.clientToken.core.validation.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

internal data class TokenCheckStatusDataResponse(
    val success: Boolean?
) : JSONDeserializable {
    companion object {
        private const val SUCCESS_FIELD = "success"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            TokenCheckStatusDataResponse(
                t.optBoolean(SUCCESS_FIELD)
            )
        }
    }
}
