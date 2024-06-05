package io.primer.android.domain.error.models

import io.primer.android.data.base.exceptions.IllegalValueKey
import java.util.UUID

internal sealed class GeneralError : PrimerError() {

    object MissingConfigurationError : GeneralError()

    class InvalidValueError(val illegalValueKey: IllegalValueKey, val message: String?) :
        GeneralError()

    class InvalidClientSessionValueError(
        val illegalValueKey: IllegalValueKey,
        val value: Any?,
        val allowedValue: Any?,
        val message: String?
    ) : GeneralError()

    class InvalidUrlError(val message: String) : GeneralError()

    class UnknownError(val message: String) : GeneralError()

    override val errorId: String
        get() = when (this) {
            is UnknownError -> "unknown-error"
            is InvalidUrlError -> "invalid-url"
            is MissingConfigurationError -> "missing-configuration"
            is InvalidValueError -> "invalid-value"
            is InvalidClientSessionValueError -> "invalid-client-session-value"
        }

    override val description: String
        get() = when (this) {
            is UnknownError -> "Something went wrong. Message $message."
            is InvalidUrlError -> "$message."
            is MissingConfigurationError -> "Missing SDK configuration."
            is InvalidValueError -> "Invalid value for '${illegalValueKey.key}'. Message $message."
            is InvalidClientSessionValueError ->
                "Invalid client session value for '${illegalValueKey.key}' with value '$value'"
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val errorCode: String? = null

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is UnknownError, is InvalidUrlError -> "Contact Primer and provide us with diagnostics id $diagnosticsId"
            is MissingConfigurationError ->
                "Check if you have an active internet connection."
            is InvalidValueError ->
                "Contact Primer and provide us with diagnostics id $diagnosticsId"
            is InvalidClientSessionValueError ->
                listOfNotNull(
                    "Check if you have provided a valid value " +
                        "for ${illegalValueKey.key} in your client session",
                    allowedValue?.let { "Allowed values are [$allowedValue]" }
                ).joinToString()
        }
}
