package io.primer.android.session

import android.util.Base64
import io.primer.android.model.json
import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException

@Serializable
internal data class ClientToken(
    val configurationUrl: String,
    val accessToken: String,
) {
    companion object {
        fun fromString(encoded: String): ClientToken {
            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.DEFAULT)
                val decoded = String(bytes)

                if (decoded.contains("\"accessToken\":")) {
                    return json.decodeFromString(serializer(), decoded)
                }
            }

            // TODO: clean this up
            throw IllegalArgumentException()
        }
    }
}