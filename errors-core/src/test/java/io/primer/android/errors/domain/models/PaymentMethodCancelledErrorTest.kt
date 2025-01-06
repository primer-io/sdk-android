package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.ErrorContextParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PaymentMethodCancelledErrorTest {
    @Test
    fun `should create PaymentMethodCancelledError with correct properties`() {
        // Arrange
        val paymentMethodType = "credit-card"

        // Act
        val error = PaymentMethodCancelledError(paymentMethodType)

        // Assert
        assertEquals("payment-cancelled", error.errorId)
        assertEquals(
            "Vaulting/Checking out for $paymentMethodType was cancelled by the user.",
            error.description,
        )
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertTrue(error.context is ErrorContextParams)
        assertNull(error.recoverySuggestion)
    }
}
