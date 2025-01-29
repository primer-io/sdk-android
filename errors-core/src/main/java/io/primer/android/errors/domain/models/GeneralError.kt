package io.primer.android.errors.domain.models

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.data.exception.IllegalValueKey
import java.util.UUID

internal sealed class GeneralError : PrimerError() {
    data object MissingConfigurationError : GeneralError()

    class InvalidValueError(
        val illegalValueKey: IllegalValueKey,
        val message: String?,
    ) : GeneralError()

    class InvalidClientSessionValueError(
        val illegalValueKey: IllegalValueKey,
        val value: Any?,
        val allowedValue: Any?,
        val message: String?,
    ) : GeneralError()

    class InvalidUrlError(val message: String) : GeneralError()

    class UnhandledPaymentPendingStateError(val message: String) : GeneralError()

    override val errorId: String
        get() =
            when (this) {
                is InvalidUrlError -> "invalid-url"
                is MissingConfigurationError -> "missing-configuration"
                is InvalidValueError -> "invalid-value"
                is InvalidClientSessionValueError -> "invalid-client-session-value"
                is UnhandledPaymentPendingStateError -> "unhandled-pending-state"
            }

    override val description: String
        get() =
            when (this) {
                is InvalidUrlError -> "$message."
                is MissingConfigurationError -> "Missing SDK configuration."
                is InvalidValueError -> "Invalid value for '${illegalValueKey.key}'. Message $message."
                is InvalidClientSessionValueError ->
                    "Invalid client session value for '${illegalValueKey.key}' with value '$value'"

                is UnhandledPaymentPendingStateError -> "$message."
            }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val errorCode: String? = null

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() =
            when (this) {
                is InvalidUrlError -> "Contact Primer and provide us with diagnostics id $diagnosticsId."
                is MissingConfigurationError -> "Check if you have an active internet connection."
                is InvalidValueError ->
                    "Contact Primer and provide us with diagnostics id $diagnosticsId"

                is InvalidClientSessionValueError ->
                    listOfNotNull(
                        "Check if you have provided a valid value " +
                            "for ${illegalValueKey.key} in your client session",
                        allowedValue?.let { "Allowed values are [$allowedValue]" },
                    ).joinToString()

                is UnhandledPaymentPendingStateError -> "Implementation error."
            }
}
