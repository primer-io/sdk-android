package io.primer.android.errors.domain.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServerErrorTest {

    @Test
    fun `should create ServerError with correct properties`() {
        // Arrange
        val description = "An error occurred on the server."
        val diagnosticsId = "12345-abcde"

        // Act
        val error = ServerError(description, diagnosticsId)

        // Assert
        assertEquals("server-error", error.errorId)
        assertEquals(description, error.description)
        assertEquals(diagnosticsId, error.diagnosticsId)
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertEquals("Please contact Primer with diagnostics id $diagnosticsId.", error.recoverySuggestion)
    }

    @Test
    fun `diagnosticsId should be unique for each instance`() {
        // Arrange
        val description1 = "Server error 1"
        val diagnosticsId1 = "12345-abcde"
        val description2 = "Server error 2"
        val diagnosticsId2 = "67890-fghij"

        // Act
        val error1 = ServerError(description1, diagnosticsId1)
        val error2 = ServerError(description2, diagnosticsId2)

        // Assert
        assertNotEquals(error1.diagnosticsId, error2.diagnosticsId)
    }

    @Test
    fun `recovery suggestion should include diagnosticsId`() {
        // Arrange
        val description = "An error occurred on the server."
        val diagnosticsId = "12345-abcde"

        // Act
        val error = ServerError(description, diagnosticsId)

        // Assert
        assertTrue(error.recoverySuggestion.contains(diagnosticsId))
    }
}
