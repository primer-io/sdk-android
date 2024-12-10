package io.primer.sample.utils

import android.util.Base64
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException

object ClientTokenDecoder {

    @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
    fun decode(encoded: String): String {
        if (encoded.isBlank()) throw InvalidClientTokenException()

        val tokens = encoded.split(".")

        for (elm in tokens) {
            val bytes = Base64.decode(elm, Base64.URL_SAFE)
            val decoded = String(bytes)

            if (decoded.contains("\"accessToken\":")) {
                return decoded
            }
        }

        throw InvalidClientTokenException()
    }
}
