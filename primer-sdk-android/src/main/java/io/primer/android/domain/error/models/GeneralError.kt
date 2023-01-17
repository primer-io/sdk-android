package io.primer.android.domain.error.models

import io.primer.android.data.base.exceptions.IllegalValueKey
import java.util.UUID

internal sealed class GeneralError : PrimerError() {

    object MissingConfigurationError : GeneralError()

    class InvalidValueError(val key: IllegalValueKey, val message: String?) : GeneralError()

    class UnknownError(val message: String) : GeneralError()

    override val errorId: String
        get() = when (this) {
            is UnknownError -> "unknown-error"
            is MissingConfigurationError -> "missing-configuration"
            is InvalidValueError -> "invalid-value"
        }

    override val description: String
        get() = when (this) {
            is UnknownError -> "Something went wrong. Message $message."
            is MissingConfigurationError -> "Missing SDK configuration."
            is InvalidValueError -> "Invalid value for $key. Message $message."
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is UnknownError -> "Contact Primer and provide us with diagnostics id $diagnosticsId"
            is MissingConfigurationError ->
                "Check if you have an active internet connection." +
                    " Contact Primer and provide us with diagnostics id $diagnosticsId"
            is InvalidValueError ->
                "Contact Primer and provide us with diagnostics id $diagnosticsId"
        }
}
