package io.primer.android.model.dto

import android.util.Base64
import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable

private const val DIVIDER = 1000

@Serializable
internal data class ClientToken(
    val configurationUrl: String,
    val accessToken: String,
    val exp: Int,
) {

    companion object {

        val json = Serialization.json

        private fun isGreaterThan(t1: Long, t2: Long): Boolean {
            return t1 > t2
        }

        @Throws(IllegalArgumentException::class)
        fun fromString(encoded: String): ClientToken {
            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.DEFAULT)
                val decoded = String(bytes)

                if (decoded.contains("\"accessToken\":")) {
                    val token = json.decodeFromString(serializer(), decoded)

                    val currentTime = System.currentTimeMillis() / DIVIDER

                    val isExpired = isGreaterThan(currentTime, token.exp.toLong())

                    if (isExpired) throw IllegalArgumentException()

                    return token
                }
            }

            throw IllegalArgumentException()
        }
    }
}
