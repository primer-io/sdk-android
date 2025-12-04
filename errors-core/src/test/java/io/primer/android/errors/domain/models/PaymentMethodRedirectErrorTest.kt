package io.primer.android.errors.domain.models

import io.primer.android.analytics.domain.models.ErrorContextParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PaymentMethodRedirectErrorTest {
    @Test
    fun `should create PaymentMethodCancelledError with correct properties`() {
        val paymentMethodType = "web-redirect"
        val uri = "primer://sdk"

        // Act
        val error = PaymentMethodRedirectError(paymentMethodType = paymentMethodType, uri = uri)

        assertEquals("failed-to-redirect", error.errorId)
        assertEquals(
            "Failed to redirect to $uri.",
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
