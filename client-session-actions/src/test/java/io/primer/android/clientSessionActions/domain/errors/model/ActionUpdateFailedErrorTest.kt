package io.primer.android.clientSessionActions.domain.errors.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ActionUpdateFailedErrorTest {
    @Test
    fun `should create ActionUpdateFailedError with given server description and diagnostics ID`() {
        val serverDescription = "Server error occurred"
        val serverDiagnosticsId = "12345"

        val error =
            ActionUpdateFailedError(
                serverDescription,
                serverDiagnosticsId,
            )

        assertEquals("failed-to-update-client-session", error.errorId)
        assertEquals(serverDescription, error.description)
        assertEquals(serverDiagnosticsId, error.diagnosticsId)
        assertEquals("Contact Primer and provide us with diagnostics id $serverDiagnosticsId", error.recoverySuggestion)
        assertEquals(error, error.exposedError)
        assertEquals(null, error.errorCode)
    }

    @Test
    fun `should create ActionUpdateFailedError with a generated diagnostics ID when serverDiagnosticsId is null`() {
        val serverDescription = "Server error occurred"

        val error =
            ActionUpdateFailedError(serverDescription, null)

        assertEquals("failed-to-update-client-session", error.errorId)
        assertEquals(serverDescription, error.description)
        assertEquals(
            "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}",
            error.recoverySuggestion,
        )
        assertEquals(error, error.exposedError)
        assertEquals(null, error.errorCode)

        // Check that diagnosticsId is a valid UUID
        UUID.fromString(error.diagnosticsId)
    }

    @Test
    fun `should have a valid UUID when serverDiagnosticsId is null`() {
        val serverDescription = "Server error occurred"

        val error =
            ActionUpdateFailedError(serverDescription, null)

        // Check that diagnosticsId is a valid UUID
        UUID.fromString(error.diagnosticsId)
    }
}
