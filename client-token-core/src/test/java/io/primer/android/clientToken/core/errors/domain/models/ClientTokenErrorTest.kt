package io.primer.android.clientToken.core.errors.domain.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ClientTokenErrorTest {

    @Test
    fun `test InvalidClientTokenError properties`() {
        // Arrange
        val description = "The client token provided is not a valid client token."
        val error = ClientTokenError.InvalidClientTokenError(description = description)

        // Act & Assert
        assertEquals("invalid-client-token", error.errorId)
        assertEquals(description, error.description)
        assertEquals(null, error.errorCode)
        assertNotNull(error.diagnosticsId)
        assertEquals(error, error.exposedError)
        assertEquals(
            "Ensure that the client token fetched from your backend is a valid client token" +
                " (i.e. not null, not blank, is valid JWT and it comes from Primer).",
            error.recoverySuggestion
        )
    }

    @Test
    fun `test ExpiredClientTokenError properties`() {
        // Arrange
        val description = "Cannot initialize the SDK because the client token provided is expired."
        val error = ClientTokenError.ExpiredClientTokenError(description = description)

        // Act & Assert
        assertEquals("expired-client-token", error.errorId)
        assertEquals(description, error.description)
        assertEquals(null, error.errorCode)
        assertNotNull(error.diagnosticsId)
        assertEquals(error, error.exposedError)
        assertEquals(
            "Avoid storing client tokens locally." +
                " Fetch a new client token to provide on when starting Primer.",
            error.recoverySuggestion
        )
    }
}
