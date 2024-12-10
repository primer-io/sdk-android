package io.primer.android.clientToken.core.token.data.model

import android.util.Base64
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ClientToken(
    val configurationUrl: String?,
    val analyticsUrlV2: String?,
    val intent: String,
    val accessToken: String,
    val exp: Int
) : JSONDeserializable {

    companion object {
        private fun checkIfExpired(expInSeconds: Long): Boolean {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) >= expInSeconds
        }

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): ClientToken {
            if (encoded.isBlank()) throw InvalidClientTokenException()

            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.URL_SAFE)
                val decoded = String(bytes)
                if (decoded.contains("\"accessToken\":")) {
                    val token = JSONSerializationUtils.getJsonObjectDeserializer<ClientToken>()
                        .deserialize(JSONObject(decoded))

                    val isExpired = checkIfExpired(token.exp.toLong())

                    if (isExpired) throw ExpiredClientTokenException()

                    return token
                }
            }

            throw InvalidClientTokenException()
        }

        private const val CONFIGURATION_URL_FIELD = "configurationUrl"
        private const val ANALYTICS_URL_V2_FIELD = "analyticsUrlV2"
        private const val INTENT_FIELD = "intent"
        private const val ACCESS_TOKEN_FIELD = "accessToken"
        private const val EXP_FIELD = "exp"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            ClientToken(
                configurationUrl = t.optNullableString(CONFIGURATION_URL_FIELD),
                analyticsUrlV2 = t.optNullableString(ANALYTICS_URL_V2_FIELD),
                intent = t.getString(INTENT_FIELD),
                accessToken = t.getString(ACCESS_TOKEN_FIELD),
                exp = t.getInt(EXP_FIELD)
            )
        }
    }
}
