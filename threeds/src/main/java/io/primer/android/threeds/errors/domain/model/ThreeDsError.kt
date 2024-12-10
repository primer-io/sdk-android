package io.primer.android.threeds.errors.domain.model

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal sealed class ThreeDsError : PrimerError() {

    object ThreeDsLibraryMissingError : ThreeDsError()

    class ThreeDsLibraryVersionError(
        val validSdkVersion: String,
        override val context: ThreeDsFailureContextParams
    ) : ThreeDsError()

    class ThreeDsInitError(
        val message: String,
        override val context: ThreeDsFailureContextParams
    ) : ThreeDsError()

    class ThreeDsConfigurationError(
        val message: String,
        override val context: ThreeDsFailureContextParams
    ) : ThreeDsError()

    class ThreeDsUnknownProtocolError(
        val initProtocolVersion: String,
        override val context: ThreeDsFailureContextParams
    ) : ThreeDsError()

    class ThreeDsMissingDirectoryServerIdError(
        val cardNetwork: CardNetwork.Type,
        override val context: ThreeDsFailureContextParams
    ) : ThreeDsError()

    class ThreeDsChallengeCancelledError(
        override val errorCode: String?,
        internal val message: String?,
        override val context: ThreeDsRuntimeFailureContextParams
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    class ThreeDsChallengeTimedOutError(
        override val errorCode: String?,
        internal val message: String?,
        override val context: ThreeDsRuntimeFailureContextParams
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    class ThreeDsChallengeFailedError(
        internal val message: String?,
        override val context: ThreeDsRuntimeFailureContextParams
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    class ThreeDsChallengeInvalidStatusError(
        internal val transactionStatus: String,
        internal val transactionId: String,
        override val errorCode: String?,
        internal val message: String?,
        override val context: ThreeDsRuntimeFailureContextParams
    ) : ThreeDsError() {
        override val exposedError: PrimerError
            get() = this
    }

    class ThreeDsChallengeProtocolFailedError(
        override val errorCode: String,
        internal val message: String,
        override val context: ThreeDsProtocolFailureContextParams
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
            is ThreeDsUnknownProtocolError -> "3ds-unknown-protocol"
            is ThreeDsMissingDirectoryServerIdError -> "3ds-missing-directory-server-id"
            is ThreeDsChallengeFailedError,
            is ThreeDsChallengeProtocolFailedError,
            is ThreeDsChallengeInvalidStatusError -> "3ds-challenge-failed"

            is ThreeDsChallengeCancelledError -> "3ds-challenge-cancelled-by-user"
            is ThreeDsChallengeTimedOutError -> "3ds-challenge-timed-out"
            is ThreeDsLibraryMissingError -> "missing-sdk-dependency"
            is ThreeDsLibraryVersionError -> "invalid-3ds-sdk-version"
            is ThreeDsUnknownError -> "3ds-unknown-error"
        }

    override val description: String
        get() = when (this) {
            is ThreeDsInitError -> "3DS SDK init failed with errors: $message"
            is ThreeDsConfigurationError ->
                "Cannot perform 3DS due to invalid 3DS configuration. $message"

            is ThreeDsUnknownProtocolError ->
                """
                     Cannot perform 3DS due to unsupported
                     3DS protocol version $initProtocolVersion."
                """.trimIndent()

            is ThreeDsMissingDirectoryServerIdError ->
                "Cannot perform 3DS due to missing directory server RID for $cardNetwork."

            is ThreeDsChallengeCancelledError -> "3DS Challenge cancelled by user."
            is ThreeDsChallengeInvalidStatusError ->
                "3DS challenge for transaction with id " +
                    "($transactionId) failed with status ($transactionStatus)."

            is ThreeDsChallengeTimedOutError -> "3DS Challenge timed out."
            is ThreeDsChallengeFailedError -> message.orEmpty()
            is ThreeDsChallengeProtocolFailedError ->
                "3DS Challenge failed due to [$errorCode]. $message"

            is ThreeDsLibraryMissingError ->
                "Cannot perform 3DS due to missing library on classpath."

            is ThreeDsLibraryVersionError ->
                "Cannot perform 3DS due to library versions mismatch."

            is ThreeDsUnknownError ->
                "An unknown error occurred while trying to perform 3DS."
        }

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is ThreeDsLibraryMissingError ->
                "Follow the integration guide and include 3DS dependency."
            is ThreeDsLibraryVersionError ->
                "Update to io.primer:3ds-android:$validSdkVersion"
            is ThreeDsInitError -> """
                If this application is not installed from a trusted source
                (e.g. a debug version, or used on an emulator), try to set 
                'PrimerDebugOptions.is3DSSanityCheckEnabled' to false.
                Contact Primer and provide us with diagnostics id $diagnosticsId
            """.trimIndent()
            is ThreeDsUnknownProtocolError -> "Update to the newest io.primer:3ds-android version."
            is ThreeDsConfigurationError,
            is ThreeDsChallengeCancelledError,
            is ThreeDsChallengeTimedOutError,
            is ThreeDsChallengeFailedError,
            is ThreeDsChallengeInvalidStatusError,
            is ThreeDsChallengeProtocolFailedError,
            is ThreeDsUnknownError,
            is ThreeDsMissingDirectoryServerIdError
            -> "Contact Primer and provide us with diagnostics id $diagnosticsId"
        }
}
