package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class ParserError : PrimerError() {

    class EncodeError(val message: String) : ParserError()

    class DecodeError(val message: String) : ParserError()

    override val errorId: String
        get() = when (this) {
            is EncodeError -> "failed-to-decode"
            is DecodeError -> "failed-to-encode"
        }

    override val description: String
        get() = when (this) {
            is EncodeError -> "Failed to encode $message"
            is DecodeError -> "Failed to decode $message"
        }

    override val errorCode: String? = null

    override val exposedError = GeneralError.UnknownError(
        "Unknown error occurred. " +
            "Please contact us with diagnosticsId $diagnosticsId to investigate further."
    )

    final override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val recoverySuggestion = "Check underlying message for more info."
}
