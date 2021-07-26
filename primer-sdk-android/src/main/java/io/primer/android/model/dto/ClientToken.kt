package io.primer.android.model.dto

import android.util.Base64
import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
internal data class ClientToken(
    val configurationUrl: String,
    val accessToken: String,
    val exp: Int,
) {

    companion object {

        val json = Serialization.json

        private fun checkIfExpired(expInSeconds: Long): Boolean {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) >= expInSeconds
        }

        @Throws(IllegalArgumentException::class)
        fun fromString(encoded: String): ClientToken {
            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.DEFAULT)
                val decoded = String(bytes)

                if (decoded.contains("\"accessToken\":")) {
                    val token = json.decodeFromString(serializer(), decoded)

                    val isExpired = checkIfExpired(token.exp.toLong())

                    if (isExpired) throw IllegalArgumentException("client token has expired.")

                    return token
                }
            }

            throw IllegalArgumentException("client token is invalid.")
        }
    }
}
