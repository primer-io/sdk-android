package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class ThreeDsError : PrimerError() {

    object ThreeDsLibraryMissingError : ThreeDsError()

    class ThreeDsLibraryVersionError(val validSdkVersion: String) : ThreeDsError()

    class ThreeDsInitError(val message: String) : ThreeDsError()

    class ThreeDsConfigurationError(val message: String) : ThreeDsError()

    class ThreeDsChallengeFailedError(
        internal val reason: String?,
        internal val message: String?
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
            is ThreeDsLibraryMissingError -> "missing-3ds-library"
            is ThreeDsLibraryVersionError -> "invalid-3ds-library-version"
            is ThreeDsUnknownError -> "3ds-unknown-error"
        }

    override val description: String
        get() = when (this) {
            is ThreeDsInitError -> "Cannot perform 3DS due to security reasons. $message"
            is ThreeDsConfigurationError ->
                "Cannot perform 3DS due to invalid configuration. $message"
            is ThreeDsChallengeFailedError -> "3DS Challenge failed due to $reason. $message"
            is ThreeDsLibraryMissingError ->
                "Cannot perform 3DS due to missing library on classpath."
            is ThreeDsLibraryVersionError ->
                "Cannot perform 3DS due to library versions mismatch."
            is ThreeDsUnknownError -> "An unknown error occurred while trying to perform 3DS."
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is ThreeDsLibraryMissingError ->
                "Please follow the integration guide and include 3DS library."
            is ThreeDsLibraryVersionError ->
                "Please update to io.primer:3ds-android:$validSdkVersion"
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
