package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.threeds.data.models.CardNetwork
import java.util.UUID

internal sealed class ThreeDsError : PrimerError() {

    object ThreeDsLibraryMissingError : ThreeDsError()

    class ThreeDsLibraryVersionError(val validSdkVersion: String) : ThreeDsError()

    class ThreeDsInitError(val message: String) : ThreeDsError()

    class ThreeDsConfigurationError(val message: String) : ThreeDsError()

    class ThreeDsMissingDirectoryServerIdError(val cardNetwork: CardNetwork) : ThreeDsError()

    class ThreeDsChallengeFailedError(
        internal val errorCode: String?,
        internal val message: String?
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    class ThreeDsChallengeProtocolFailedError(
        internal val errorCode: String,
        internal val message: String,
        override val context: ThreeDsFailureContextParams
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
            is ThreeDsMissingDirectoryServerIdError -> "3ds-missing-directory-server-id"
            is ThreeDsChallengeFailedError, is ThreeDsChallengeProtocolFailedError ->
                "3ds-challenge-failed"
            is ThreeDsLibraryMissingError -> "missing-3ds-library"
            is ThreeDsLibraryVersionError -> "invalid-3ds-library-version"
            is ThreeDsUnknownError -> "3ds-unknown-error"
        }

    override val description: String
        get() = when (this) {
            is ThreeDsInitError -> "Primer3DS: Cannot perform 3DS due to security reasons. $message"
            is ThreeDsConfigurationError ->
                "Primer3DS: Cannot perform 3DS due to invalid configuration. $message"
            is ThreeDsMissingDirectoryServerIdError ->
                "Primer3DS: Cannot perform 3DS due to missing directory server for $cardNetwork"
            is ThreeDsChallengeFailedError ->
                "Primer3DS: 3DS Challenge failed due to [$errorCode]. $message"
            is ThreeDsChallengeProtocolFailedError ->
                "Primer3DS: 3DS Challenge failed due to [$errorCode]. $message"
            is ThreeDsLibraryMissingError ->
                "Primer3DS: Cannot perform 3DS due to missing library on classpath."
            is ThreeDsLibraryVersionError ->
                "Primer3DS: Cannot perform 3DS due to library versions mismatch."
            is ThreeDsUnknownError ->
                "Primer3DS: An unknown error occurred while trying to perform 3DS."
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
            is ThreeDsInitError -> """
                If this application is not installed from a trusted source
                (e.g. a debug version, or used on an emulator), try to set
                'PrimerDebugOptions.is3DSSanityCheckEnabled' to false.
                Contact Primer and provide us with diagnostics id $diagnosticsId
            """.trimIndent()
            is ThreeDsConfigurationError,
            is ThreeDsChallengeFailedError,
            is ThreeDsChallengeProtocolFailedError,
            is ThreeDsUnknownError,
            is ThreeDsMissingDirectoryServerIdError
            -> "Contact Primer and provide us with diagnostics id $diagnosticsId"
        }
}
