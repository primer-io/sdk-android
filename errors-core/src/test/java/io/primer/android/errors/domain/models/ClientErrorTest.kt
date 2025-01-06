package io.primer.android.errors.domain.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientErrorTest {
    @Test
    fun `should create ClientError with given description and diagnostics ID`() {
        // Arrange
        val description = "A client error occurred"
        val diagnosticsId = "12345"

        // Act
        val error = ClientError(description, diagnosticsId)

        // Assert
        assertEquals("client-error", error.errorId)
        assertEquals(description, error.description)
        assertEquals(diagnosticsId, error.diagnosticsId)
        assertEquals("Please contact Primer with diagnostics id $diagnosticsId.", error.recoverySuggestion)
        assertEquals(error, error.exposedError)
        assertEquals(null, error.errorCode)
    }
}
