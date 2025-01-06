package io.primer.android.errors.domain.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParserErrorTest {
    @Test
    fun `should create EncodeError with correct properties`() {
        // Arrange
        val message = "Encoding failed"

        // Act
        val error = ParserError.EncodeError(message)

        // Assert
        assertEquals("failed-to-encode", error.errorId)
        assertEquals("Failed to encode $message", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertTrue(
            error.exposedError.message.contains("Please contact us with diagnosticsId"),
        )
        assertEquals("Check underlying message for more info.", error.recoverySuggestion)
    }

    @Test
    fun `should create DecodeError with correct properties`() {
        // Arrange
        val message = "Decoding failed"

        // Act
        val error = ParserError.DecodeError(message)

        // Assert
        assertEquals("failed-to-decode", error.errorId)
        assertEquals("Failed to decode $message", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertTrue(
            error.exposedError.message.contains("Please contact us with diagnosticsId"),
        )
        assertEquals("Check underlying message for more info.", error.recoverySuggestion)
    }

    @Test
    fun `diagnosticsId should be unique for each instance`() {
        // Act
        val encodeError1 = ParserError.EncodeError("message1")
        val encodeError2 = ParserError.EncodeError("message2")
        val decodeError1 = ParserError.DecodeError("message3")
        val decodeError2 = ParserError.DecodeError("message4")

        // Assert
        assertNotEquals(encodeError1.diagnosticsId, encodeError2.diagnosticsId)
        assertNotEquals(decodeError1.diagnosticsId, decodeError2.diagnosticsId)
        assertNotEquals(encodeError1.diagnosticsId, decodeError1.diagnosticsId)
    }

    @Test
    fun `should have correct recovery suggestion`() {
        // Act
        val encodeError = ParserError.EncodeError("Encoding message")
        val decodeError = ParserError.DecodeError("Decoding message")

        // Assert
        assertEquals("Check underlying message for more info.", encodeError.recoverySuggestion)
        assertEquals("Check underlying message for more info.", decodeError.recoverySuggestion)
    }

    @Test
    fun `exposedError should be a GeneralError_UnknownError with correct message`() {
        // Act
        val encodeError = ParserError.EncodeError("Encoding message")
        val decodeError = ParserError.DecodeError("Decoding message")

        // Assert
        val encodeExposedError = encodeError.exposedError
        val decodeExposedError = decodeError.exposedError

        assertTrue(encodeExposedError.message.contains("Unknown error occurred."))
        assertTrue(decodeExposedError.message.contains("Unknown error occurred."))
        assertTrue(encodeExposedError.message.contains("Please contact us with diagnosticsId"))
        assertTrue(decodeExposedError.message.contains("Please contact us with diagnosticsId"))
    }
}
