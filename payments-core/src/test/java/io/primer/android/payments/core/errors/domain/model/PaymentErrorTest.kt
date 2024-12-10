package io.primer.android.payments.core.errors.domain.model

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.payments.core.create.data.model.PaymentStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PaymentErrorTest {

    @Test
    fun `PaymentFailedError should return correct values`() {
        // Arrange
        val paymentId = "payment123"
        val paymentStatus = PaymentStatus.FAILED
        val paymentMethodType = "credit_card"
        val paymentFailedError = PaymentError.PaymentFailedError(paymentId, paymentStatus, paymentMethodType)

        // Act & Assert
        assertEquals("payment-failed", paymentFailedError.errorId)
        assertEquals(
            "The payment with id $paymentId was created but ended up in a $paymentStatus status.",
            paymentFailedError.description
        )
        assertNull(paymentFailedError.errorCode)
        assertNotNull(paymentFailedError.diagnosticsId)
        assertEquals(paymentFailedError, paymentFailedError.exposedError)
        assertNull(paymentFailedError.recoverySuggestion)
        assertEquals(ErrorContextParams(paymentFailedError.errorId, paymentMethodType), paymentFailedError.context)
    }

    @Test
    fun `PaymentCreateFailedError should return correct values`() {
        // Arrange
        val serverDescription = "Failed to create payment"
        val serverDiagnosticsId = "diagnostics123"
        val paymentCreateFailedError = PaymentError.PaymentCreateFailedError(serverDescription, serverDiagnosticsId)

        // Act & Assert
        assertEquals("failed-to-create-payment", paymentCreateFailedError.errorId)
        assertEquals(serverDescription, paymentCreateFailedError.description)
        assertNull(paymentCreateFailedError.errorCode)
        assertEquals(serverDiagnosticsId, paymentCreateFailedError.diagnosticsId)
        assertEquals(paymentCreateFailedError, paymentCreateFailedError.exposedError)
        assertEquals(
            "Contact Primer and provide us with diagnostics id $serverDiagnosticsId",
            paymentCreateFailedError.recoverySuggestion
        )
    }

    @Test
    fun `PaymentCreateFailedError should generate diagnosticsId if null`() {
        // Arrange
        val serverDescription = "Failed to create payment"
        val paymentCreateFailedError = PaymentError.PaymentCreateFailedError(serverDescription, null)

        // Act & Assert
        assertNotNull(paymentCreateFailedError.diagnosticsId)
    }

    @Test
    fun `PaymentResumeFailedError should return correct values`() {
        // Arrange
        val serverDescription = "Failed to resume payment"
        val serverDiagnosticsId = "diagnostics123"
        val paymentResumeFailedError = PaymentError.PaymentResumeFailedError(serverDescription, serverDiagnosticsId)

        // Act & Assert
        assertEquals("failed-to-resume-payment", paymentResumeFailedError.errorId)
        assertEquals(serverDescription, paymentResumeFailedError.description)
        assertNull(paymentResumeFailedError.errorCode)
        assertEquals(serverDiagnosticsId, paymentResumeFailedError.diagnosticsId)
        assertEquals(paymentResumeFailedError, paymentResumeFailedError.exposedError)
        assertEquals(
            "Contact Primer and provide us with diagnostics id $serverDiagnosticsId",
            paymentResumeFailedError.recoverySuggestion
        )
    }

    @Test
    fun `PaymentResumeFailedError should generate diagnosticsId if null`() {
        // Arrange
        val serverDescription = "Failed to resume payment"
        val paymentResumeFailedError = PaymentError.PaymentResumeFailedError(serverDescription, null)

        // Act & Assert
        assertNotNull(paymentResumeFailedError.diagnosticsId)
    }
}
