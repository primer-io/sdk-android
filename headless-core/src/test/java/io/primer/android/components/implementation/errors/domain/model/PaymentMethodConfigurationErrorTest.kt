package io.primer.android.components.implementation.errors.domain.model

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.PrimerSessionIntent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PaymentMethodConfigurationErrorTest {

    @Test
    fun `should create MisConfiguredPaymentMethodError with correct properties`() {
        // Arrange
        val paymentMethodType = "credit-card"

        // Act
        val error = PaymentMethodConfigurationError.MisConfiguredPaymentMethodError(paymentMethodType)

        // Assert
        assertEquals("misconfigured-payment-method", error.errorId)
        assertEquals(
            "Cannot present $paymentMethodType because it has not been configured correctly.",
            error.description
        )
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertTrue(error.context is ErrorContextParams)
        assertEquals(
            "Ensure that $paymentMethodType has been configured correctly " +
                "on the dashboard (https://dashboard.primer.io/)",
            error.recoverySuggestion
        )
    }

    @Test
    fun `should create UnsupportedPaymentMethodError with correct properties`() {
        // Arrange
        val paymentMethodType = "credit-card"

        // Act
        val error = PaymentMethodConfigurationError.UnsupportedPaymentMethodError(paymentMethodType)

        // Assert
        assertEquals("unsupported-payment-method-type", error.errorId)
        assertEquals("Cannot present $paymentMethodType because it is not supported.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertTrue(error.context is ErrorContextParams)
        assertNull(error.recoverySuggestion)
    }

    @Test
    fun `should create UnsupportedIntentPaymentMethodError with correct properties`() {
        // Arrange
        val paymentMethodType = "credit-card"
        val intent = PrimerSessionIntent.CHECKOUT

        // Act
        val error = PaymentMethodConfigurationError.UnsupportedIntentPaymentMethodError(paymentMethodType, intent)

        // Assert
        assertEquals("unsupported-session-intent", error.errorId)
        assertEquals(
            "Cannot initialize the SDK because $paymentMethodType does not support $intent.",
            error.description
        )
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(error, error.exposedError)
        assertTrue(error.context is ErrorContextParams)
        assertEquals(
            "Use a different payment method for $intent, or the same payment method " +
                "with ${intent.oppositeIntent}.",
            error.recoverySuggestion
        )
    }

    @Test
    fun `diagnosticsId should be unique for each instance`() {
        // Act
        val error1 = PaymentMethodConfigurationError.MisConfiguredPaymentMethodError("credit-card")
        val error2 = PaymentMethodConfigurationError.UnsupportedIntentPaymentMethodError(
            "credit-card",
            PrimerSessionIntent.CHECKOUT
        )

        // Assert
        assertNotEquals(error1.diagnosticsId, error2.diagnosticsId)
    }
}
