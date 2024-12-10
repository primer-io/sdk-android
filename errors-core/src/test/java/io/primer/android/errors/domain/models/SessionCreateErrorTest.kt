package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.ErrorContextParams
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SessionCreateErrorTest {

    @Test
    fun `should create SessionCreateError with correct properties`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val serverDiagnosticsId = "12345-abcde"
        val serverDescription = "Server error description"

        // Act
        val error = SessionCreateError(paymentMethodType, serverDiagnosticsId, serverDescription)

        // Assert
        assertEquals("failed-to-create-session", error.errorId)
        assertEquals("Failed to create session for $paymentMethodType. $serverDescription", error.description)
        assertEquals(serverDiagnosticsId, error.diagnosticsId)
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertEquals(
            "Ensure that the $paymentMethodType is configured correctly " +
                "on the dashboard (https://dashboard.primer.io/)",
            error.recoverySuggestion
        )
        assertTrue(error.context is ErrorContextParams)
        assertEquals("failed-to-create-session", (error.context as ErrorContextParams).errorId)
        assertEquals(paymentMethodType, (error.context as ErrorContextParams).paymentMethodType)
    }

    @Test
    fun `should create SessionCreateError with generated diagnosticsId when serverDiagnosticsId is null`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val serverDescription = "Server error description"

        // Act
        val error = SessionCreateError(paymentMethodType, null, serverDescription)

        // Assert
        assertNotNull(error.diagnosticsId)
        assertDoesNotThrow { UUID.fromString(error.diagnosticsId) }
    }

    @Test
    fun `should create SessionCreateError with null serverDescription`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val serverDiagnosticsId = "12345-abcde"

        // Act
        val error = SessionCreateError(paymentMethodType, serverDiagnosticsId, null)

        // Assert
        assertEquals("Failed to create session for $paymentMethodType. null", error.description)
    }

    @Test
    fun `should create SessionCreateError with correct recovery suggestion`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val serverDiagnosticsId = "12345-abcde"
        val serverDescription = "Server error description"

        // Act
        val error = SessionCreateError(paymentMethodType, serverDiagnosticsId, serverDescription)

        // Assert
        assertEquals(
            "Ensure that the $paymentMethodType is configured correctly " +
                "on the dashboard (https://dashboard.primer.io/)",
            error.recoverySuggestion
        )
    }
}
