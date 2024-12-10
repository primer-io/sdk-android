package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal sealed class ParserError : PrimerError() {

    class EncodeError(val message: String) : ParserError()

    class DecodeError(val message: String) : ParserError()

    override val errorId: String
        get() = when (this) {
            is EncodeError -> "failed-to-encode"
            is DecodeError -> "failed-to-decode"
        }

    override val description: String
        get() = when (this) {
            is EncodeError -> "Failed to encode $message"
            is DecodeError -> "Failed to decode $message"
        }

    override val errorCode: String? = null

    override val exposedError = PrimerUnknownError(
        "Unknown error occurred. " +
            "Please contact us with diagnosticsId $diagnosticsId to investigate further."
    )

    final override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val recoverySuggestion = "Check underlying message for more info."
}
