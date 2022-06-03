package io.primer.android.data.token.model

import android.util.Base64
import io.primer.android.data.token.exception.ExpiredClientTokenException
import io.primer.android.data.token.exception.InvalidClientTokenException
import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
internal data class ClientToken(
    val configurationUrl: String? = null,
    val analyticsUrlV2: String? = null,
    val intent: ClientTokenIntent,
    val accessToken: String,
    val exp: Int,
    val statusUrl: String? = null,
    val redirectUrl: String? = null,
    val qrCode: String? = null
) {

    companion object {

        val json = Serialization.json

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
                    val token = json.decodeFromString(serializer(), decoded)

                    val isExpired = checkIfExpired(token.exp.toLong())

                    if (isExpired) throw ExpiredClientTokenException()

                    return token
                }
            }

            throw InvalidClientTokenException()
        }
    }
}
