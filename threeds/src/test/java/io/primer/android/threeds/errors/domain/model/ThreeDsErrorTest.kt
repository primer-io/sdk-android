package io.primer.android.threeds.errors.domain.model

import io.mockk.mockk
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import io.primer.android.configuration.data.model.CardNetwork
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ThreeDsErrorTest {

    private val mockContextParams: ThreeDsFailureContextParams = object : ThreeDsFailureContextParams(
        threeDsSdkVersion = "1.0.0",
        initProtocolVersion = "2.1.0",
        threeDsWrapperSdkVersion = "1.0.0",
        threeDsSdkProvider = "Primer"
    ) {}
    private val mockProtocolContextParams: ThreeDsProtocolFailureContextParams = mockk()
    private val mockRuntimeContextParams: ThreeDsRuntimeFailureContextParams = mockk()
    private val mockCardNetwork = CardNetwork.Type.VISA
    private val mockInitProtocolVersion = "2.1.0"
    private val mockMessage = "Mock error message"
    private val mockValidSdkVersion = "1.0.0"

    @Nested
    inner class ErrorTypesTests {

        @Test
        fun `ThreeDsLibraryMissingError should have correct properties`() {
            val error = ThreeDsError.ThreeDsLibraryMissingError
            assertEquals("missing-sdk-dependency", error.errorId)
            assertEquals("Cannot perform 3DS due to missing library on classpath.", error.description)
            assertEquals("Follow the integration guide and include 3DS dependency.", error.recoverySuggestion)
        }

        @Test
        fun `ThreeDsLibraryVersionError should have correct properties`() {
            val error = ThreeDsError.ThreeDsLibraryVersionError(mockValidSdkVersion, mockContextParams)
            assertEquals("invalid-3ds-sdk-version", error.errorId)
            assertEquals("Cannot perform 3DS due to library versions mismatch.", error.description)
            assertEquals("Update to io.primer:3ds-android:$mockValidSdkVersion", error.recoverySuggestion)
        }

        @Test
        fun `ThreeDsInitError should have correct properties`() {
            val error = ThreeDsError.ThreeDsInitError(mockMessage, mockContextParams)
            assertEquals("3ds-init-error", error.errorId)
            assertEquals("3DS SDK init failed with errors: $mockMessage", error.description)
            assertEquals(
                """
                If this application is not installed from a trusted source
                (e.g. a debug version, or used on an emulator), try to set 
                'PrimerDebugOptions.is3DSSanityCheckEnabled' to false.
                Contact Primer and provide us with diagnostics id ${error.diagnosticsId}
                """
                    .trimIndent(),
                error.recoverySuggestion
            )
        }

        @Test
        fun `ThreeDsConfigurationError should have correct properties`() {
            val error = ThreeDsError.ThreeDsConfigurationError(mockMessage, mockContextParams)
            assertEquals("3ds-invalid-configuration", error.errorId)
            assertEquals("Cannot perform 3DS due to invalid 3DS configuration. $mockMessage", error.description)
            assertEquals(
                "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}",
                error.recoverySuggestion
            )
        }

        @Test
        fun `ThreeDsUnknownProtocolError should have correct properties`() {
            val error = ThreeDsError.ThreeDsUnknownProtocolError(mockInitProtocolVersion, mockContextParams)
            assertEquals("3ds-unknown-protocol", error.errorId)
            assertEquals(
                """
                     Cannot perform 3DS due to unsupported
                     3DS protocol version $mockInitProtocolVersion."
                """.trimIndent(),
                error.description
            )
            assertEquals("Update to the newest io.primer:3ds-android version.", error.recoverySuggestion)
        }

        @Test
        fun `ThreeDsMissingDirectoryServerIdError should have correct properties`() {
            val error = ThreeDsError.ThreeDsMissingDirectoryServerIdError(mockCardNetwork, mockContextParams)
            assertEquals("3ds-missing-directory-server-id", error.errorId)
            assertEquals(
                "Cannot perform 3DS due to missing directory server RID for $mockCardNetwork.",
                error.description
            )
            assertEquals(
                "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}",
                error.recoverySuggestion
            )
        }

        @Test
        fun `ThreeDsChallengeCancelledError should have correct properties`() {
            val error = ThreeDsError.ThreeDsChallengeCancelledError(null, mockMessage, mockRuntimeContextParams)
            assertEquals("3ds-challenge-cancelled-by-user", error.errorId)
            assertEquals("3DS Challenge cancelled by user.", error.description)
            assertEquals(error, error.exposedError)
        }

        @Test
        fun `ThreeDsChallengeTimedOutError should have correct properties`() {
            val error = ThreeDsError.ThreeDsChallengeTimedOutError(null, mockMessage, mockRuntimeContextParams)
            assertEquals("3ds-challenge-timed-out", error.errorId)
            assertEquals("3DS Challenge timed out.", error.description)
            assertEquals(error, error.exposedError)
        }

        @Test
        fun `ThreeDsChallengeFailedError should have correct properties`() {
            val error = ThreeDsError.ThreeDsChallengeFailedError(mockMessage, mockRuntimeContextParams)
            assertEquals("3ds-challenge-failed", error.errorId)
            assertEquals(mockMessage, error.description)
            assertEquals(error, error.exposedError)
        }

        @Test
        fun `ThreeDsChallengeInvalidStatusError should have correct properties`() {
            val error = ThreeDsError.ThreeDsChallengeInvalidStatusError(
                "mockStatus",
                "mockTransactionId",
                null,
                mockMessage,
                mockRuntimeContextParams
            )
            assertEquals("3ds-challenge-failed", error.errorId)
            assertEquals(
                "3DS challenge for transaction with id (mockTransactionId) failed with status (mockStatus).",
                error.description
            )
            assertEquals(error, error.exposedError)
        }

        @Test
        fun `ThreeDsChallengeProtocolFailedError should have correct properties`() {
            val error = ThreeDsError.ThreeDsChallengeProtocolFailedError(
                "mockErrorCode",
                mockMessage,
                mockProtocolContextParams
            )
            assertEquals("3ds-challenge-failed", error.errorId)
            assertEquals("3DS Challenge failed due to [mockErrorCode]. $mockMessage", error.description)
            assertEquals(error, error.exposedError)
        }

        @Test
        fun `ThreeDsUnknownError should have correct properties`() {
            val error = ThreeDsError.ThreeDsUnknownError
            assertEquals("3ds-unknown-error", error.errorId)
            assertEquals("An unknown error occurred while trying to perform 3DS.", error.description)
            assertEquals(
                "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}",
                error.recoverySuggestion
            )
        }
    }
}
