package io.primer.android.errors.domain.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class UnauthorizedErrorTest {
    @Test
    fun `should create UnauthorizedError with correct properties`() {
        // Arrange
        val diagnosticsId = UUID.randomUUID().toString()

        // Act
        val error = UnauthorizedError(diagnosticsId)

        // Assert
        assertEquals("unauthorized", error.errorId)
        assertEquals("Failed to perform .... with the client token provided.", error.description)
        assertEquals(diagnosticsId, error.diagnosticsId)
        assertNull(error.errorCode)
        assertEquals("Request a new client token and provide it on the SDK.", error.recoverySuggestion)
        assertEquals(error, error.exposedError)
    }
}
