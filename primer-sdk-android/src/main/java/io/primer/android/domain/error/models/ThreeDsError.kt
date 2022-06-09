package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class ThreeDsError : PrimerError() {

    object ThreeDsLibraryError : ThreeDsError()

    class ThreeDsInitError(val message: String) : ThreeDsError()

    class ThreeDsConfigurationError(val message: String) : ThreeDsError()

    class ThreeDsChallengeFailedError(
        internal val reason: String?,
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    object ThreeDsUnknownError : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    override val errorId: String
        get() = when (this) {
            is ThreeDsConfigurationError -> "3ds-invalid-configuration"
            is ThreeDsInitError -> "3ds-init-error"
            is ThreeDsChallengeFailedError -> "3ds-challenge-failed"
            is ThreeDsLibraryError -> "missing-3ds-library"
            is ThreeDsUnknownError -> "3ds-unknown-error"
        }

    override val description: String
        get() = when (this) {
            is ThreeDsInitError -> "Cannot perform 3DS due to security reasons. $message"
            is ThreeDsConfigurationError ->
                "Cannot perform 3DS due to invalid configuration. $message"
            is ThreeDsChallengeFailedError -> reason ?: "3DS Challenge failed."
            is ThreeDsLibraryError -> "Cannot perform 3DS due to missing library on classpath."
            is ThreeDsUnknownError -> "An unknown error occurred while trying to perform 3DS."
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is ThreeDsLibraryError -> "Please follow the integration guide and include 3DS library."
            is ThreeDsInitError ->
                "In case you are using emulator you may need to set " +
                    "PrimerDebugOptions.is3DSSanityCheckEnabled to false. " +
                    "Contact Primer and provide us with diagnostics id $diagnosticsId"
            is ThreeDsConfigurationError,
            is ThreeDsChallengeFailedError,
            is ThreeDsUnknownError,
            -> "Contact Primer and provide us with diagnostics id $diagnosticsId"
        }
}
