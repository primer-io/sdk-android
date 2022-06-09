package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class GeneralError : PrimerError() {

    object MissingConfigurationError : GeneralError()

    class UnknownError(
        override val diagnosticsId: String,
        override val description: String =
            "Contact Primer and provide them diagnostics id $diagnosticsId",
        override val recoverySuggestion: String? = null
    ) : GeneralError()

    override val errorId: String
        get() = when (this) {
            is UnknownError -> "unknown-error"
            is MissingConfigurationError -> "missing-configuration"
        }

    override val description: String
        get() = when (this) {
            is UnknownError -> "Something went wrong."
            is MissingConfigurationError -> "Missing SDK configuration."
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is UnknownError -> "Contact Primer and provide them diagnostics id $diagnosticsId"
            is MissingConfigurationError ->
                "Check if you have an active internet connection." +
                    " Contact Primer and provide them diagnostics id $diagnosticsId"
        }
}
