package io.primer.android.threeds.errors.domain.model

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID
import java.util.stream.Stream

@TestInstance(Lifecycle.PER_CLASS)
internal class ThreeDsErrorTest {

    @ParameterizedTest
    @MethodSource("errorProvider")
    fun `ThreeDsError properties should be correct`(
        error: ThreeDsError,
        expectedErrorId: String,
        expectedDescription: String,
        expectedRecoverySuggestion: String,
        expectedContext: ErrorContextParams?,
    ) {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"

        assertEquals(expectedErrorId, error.errorId)
        assertEquals(expectedDescription, error.description)
        assertEquals(expectedRecoverySuggestion, error.recoverySuggestion)

        if (expectedContext != null) {
            assertEquals(expectedContext, error.context, "Error context should match expected.")
        } else {
            assertNull(error.context, "Error context should be null.")
        }

        unmockkStatic(UUID::class)
    }

    private companion object {
        @JvmStatic
        fun errorProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(
                ThreeDsError.ThreeDsLibraryMissingError,
                "missing-sdk-dependency",
                "Cannot perform 3DS due to missing library on classpath.",
                "Follow the integration guide and include 3DS dependency.",
                ErrorContextParams(errorId = "missing-sdk-dependency"),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsLibraryVersionError(
                    validSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                ),
                "invalid-3ds-sdk-version",
                "Cannot perform 3DS due to library versions mismatch.",
                "Update to io.primer:3ds-android:2.2.1.",
                ThreeDsFailureContextParams(
                    errorId = "invalid-3ds-sdk-version",
                    threeDsSdkVersion = null,
                    initProtocolVersion = null,
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.0",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsInitError(
                    message = "Initialization failed",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.2.1",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                ),
                "3ds-init-error",
                "3DS SDK init failed with errors: Initialization failed",
                """
                    If this application is not installed from a trusted source
                    (e.g. a debug version, or used on an emulator), try to set 
                    'PrimerDebugOptions.is3DSSanityCheckEnabled' to false.
                    Contact Primer and provide us with diagnostics id uuid.
                """.trimIndent(),
                ThreeDsFailureContextParams(
                    errorId = "3ds-init-error",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = null,
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.2.1",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsConfigurationError(
                    message = "Invalid config",
                    threeDsWrapperSdkVersion = "2.2.1",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                ),
                "3ds-invalid-configuration",
                "Cannot perform 3DS due to invalid 3DS configuration. Invalid config",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsFailureContextParams(
                    errorId = "3ds-invalid-configuration",
                    threeDsSdkVersion = null,
                    initProtocolVersion = null,
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.2.1",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsUnknownProtocolError(
                    initProtocolVersion = "2.0.0",
                    threeDsWrapperSdkVersion = "2.2.1",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                ),
                "3ds-unknown-protocol",
                """
                    Cannot perform 3DS due to unsupported
                    3DS protocol version 2.0.0."
                """.trimIndent(),
                "Update to the newest io.primer:3ds-android version.",
                ThreeDsFailureContextParams(
                    errorId = "3ds-unknown-protocol",
                    threeDsSdkVersion = null,
                    initProtocolVersion = null,
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.2.1",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsMissingDirectoryServerIdError(
                    cardNetwork = CardNetwork.Type.JCB,
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.2.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                ),
                "3ds-missing-directory-server-id",
                "Cannot perform 3DS due to missing directory server RID for JCB.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsFailureContextParams(
                    errorId = "3ds-missing-directory-server-id",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = null,
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.2.2",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsChallengeCancelledError(
                    message = "Challenge cancelled",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    initProtocolVersion = "2.2.0",
                    threeDsErrorCode = "-5",
                ),
                "3ds-challenge-cancelled-by-user",
                "3DS Challenge cancelled by user.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsRuntimeFailureContextParams(
                    errorId = "3ds-challenge-cancelled-by-user",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = "2.2.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsErrorCode = "-5",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsChallengeTimedOutError(
                    message = "Initialization failed",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    initProtocolVersion = "2.2.0",
                    threeDsErrorCode = "-3",
                ),
                "3ds-challenge-timed-out",
                "3DS Challenge timed out.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsRuntimeFailureContextParams(
                    errorId = "3ds-challenge-timed-out",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = "2.2.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsErrorCode = "-3",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsChallengeFailedError(
                    message = "3DS Challenge failed.",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    initProtocolVersion = "2.2.0",
                    threeDsErrorCode = "2100",
                ),
                "3ds-challenge-failed",
                "3DS Challenge failed.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsRuntimeFailureContextParams(
                    errorId = "3ds-challenge-failed",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = "2.2.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsErrorCode = "2100",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsChallengeInvalidStatusError(
                    message = "3DS Challenge failed.",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    initProtocolVersion = "2.2.0",
                    threeDsErrorCode = "100",
                    transactionStatus = "N",
                    transactionId = "1234",
                ),
                "3ds-challenge-failed",
                "3DS challenge for transaction with id (1234) failed with status (N).",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsRuntimeFailureContextParams(
                    errorId = "3ds-challenge-failed",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = "2.2.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsErrorCode = "100",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsChallengeProtocolFailedError(
                    message = "3DS Challenge failed.",
                    threeDsSdkVersion = "2.2.1",
                    threeDsWrapperSdkVersion = "2.1.2",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    initProtocolVersion = "2.2.0",
                    threeDsErrorCode = "100",
                    threeDsComponent = "A",
                    threeDsDescription = "Something went wrong.",
                    threeDsErrorMessageType = "ARes",
                    threeDsErrorDetails = "Invalid locale.",
                    threeDsProtocolVersion = "2.2.0",
                    threeDsTransactionId = "1234",
                ),
                "3ds-challenge-failed",
                "3DS Challenge failed due to [100]. 3DS Challenge failed.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ThreeDsProtocolFailureContextParams(
                    errorId = "3ds-challenge-failed",
                    threeDsSdkVersion = "2.2.1",
                    initProtocolVersion = "2.2.0",
                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    threeDsWrapperSdkVersion = "2.1.2",
                    transactionId = "1234",
                    errorCode = "100",
                    errorDetails = "Invalid locale.",
                    component = "A",
                    errorType = "ARes",
                    description = "Something went wrong.",
                    version = "2.2.0",
                ),
            ),
            Arguments.of(
                ThreeDsError.ThreeDsUnknownError,
                "3ds-unknown-error",
                "An unknown error occurred while trying to perform 3DS.",
                "Contact Primer and provide us with diagnostics id uuid.",
                ErrorContextParams(errorId = "3ds-unknown-error"),
            ),
        )
    }
}
