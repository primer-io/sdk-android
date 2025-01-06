package io.primer.android.errors.domain.models

import io.mockk.every
import io.mockk.mockk
import io.primer.android.errors.data.exception.IllegalValueKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeneralErrorTest {
    @Test
    fun `should create MissingConfigurationError with correct properties`() {
        // Act
        val error = GeneralError.MissingConfigurationError

        // Assert
        assertEquals("missing-configuration", error.errorId)
        assertEquals("Missing SDK configuration.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals("Check if you have an active internet connection.", error.recoverySuggestion)
    }

    @Test
    fun `should create InvalidValueError with correct properties`() {
        // Arrange
        val key =
            mockk<IllegalValueKey>(relaxed = true) {
                every { key } returns "invalid-key"
            }
        val message = "Invalid value provided"

        // Act
        val error = GeneralError.InvalidValueError(key, message)

        // Assert
        assertEquals("invalid-value", error.errorId)
        assertEquals("Invalid value for 'invalid-key'. Message Invalid value provided.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(
            "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}",
            error.recoverySuggestion,
        )
    }

    @Test
    fun `should create InvalidClientSessionValueError with correct properties`() {
        // Arrange
        val key =
            mockk<IllegalValueKey>(relaxed = true) {
                every { key } returns "session-key"
            }
        val value = "invalid-value"
        val allowedValue = "allowed-value"
        val message = "Invalid session value"

        // Act
        val error = GeneralError.InvalidClientSessionValueError(key, value, allowedValue, message)

        // Assert
        assertEquals("invalid-client-session-value", error.errorId)
        assertEquals(
            "Invalid client session value for 'session-key' with value 'invalid-value'",
            error.description,
        )
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(
            "Check if you have provided a valid value for session-key in your client " +
                "session, Allowed values are [allowed-value]",
            error.recoverySuggestion,
        )
    }

    @Test
    fun `should create InvalidUrlError with correct properties`() {
        // Arrange
        val message = "Invalid URL format"

        // Act
        val error = GeneralError.InvalidUrlError(message)

        // Assert
        assertEquals("invalid-url", error.errorId)
        assertEquals("Invalid URL format.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(
            "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}.",
            error.recoverySuggestion,
        )
    }

    @Test
    fun `should create UnknownError with correct properties`() {
        // Arrange
        val message = "An unknown error occurred"

        // Act
        val error = PrimerUnknownError(message)

        // Assert
        assertEquals("unknown-error", error.errorId)
        assertEquals("Something went wrong. Message An unknown error occurred.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(
            "Contact Primer and provide us with diagnostics id ${error.diagnosticsId}.",
            error.recoverySuggestion,
        )
    }
}
