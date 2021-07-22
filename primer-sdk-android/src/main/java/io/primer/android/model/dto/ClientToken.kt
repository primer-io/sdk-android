package io.primer.android.model.dto

import android.util.Base64
import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable

@Serializable
internal data class ClientToken(
    val configurationUrl: String,
    val accessToken: String,
    val exp: Int,
) {

    companion object {

        val json = Serialization.json

        private fun isSmallerThan(t1: Long, t2: Long): Boolean {
            return t1 < t2
        }

        fun fromString(encoded: String): ClientToken {
            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.DEFAULT)
                val decoded = String(bytes)

                if (decoded.contains("\"accessToken\":")) {
                    val token = json.decodeFromString(serializer(), decoded)

                    val currentTime = System.currentTimeMillis() / 1000

                    val isExpired = isSmallerThan(token.exp.toLong(), currentTime)

                    if (isExpired) throw IllegalArgumentException()

                    return token
                }
            }

            throw IllegalArgumentException()
        }
    }
}
