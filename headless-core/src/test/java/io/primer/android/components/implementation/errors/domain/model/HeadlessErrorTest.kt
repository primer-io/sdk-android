package io.primer.android.components.implementation.errors.domain.model

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.otp.PrimerOtpData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HeadlessErrorTest {

    @Test
    fun `should create InitializationError with correct properties`() {
        // Arrange
        val message = "Initialization failed"

        // Act
        val error = HeadlessError.InitializationError(message)

        // Assert
        assertEquals("huc-initialization-failed", error.errorId)
        assertEquals("PrimerHeadlessUniversalCheckout initialization failed | Message: $message", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals("Ensure you are calling 'start' method before calling this method.", error.recoverySuggestion)
        assertSame(error, error.exposedError)
    }

    @Test
    fun `should create InvalidRawDataError with correct properties`() {
        // Act
        val error = HeadlessError.InvalidRawDataError

        // Assert
        assertEquals("invalid-raw-data", error.errorId)
        assertEquals("Missing raw data.", error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertNull(error.recoverySuggestion)
        assertSame(error, error.exposedError)
    }

    @Test
    fun `should create InvalidTokenizationInputDataError with correct properties`() {
        // Arrange
        val paymentMethodType = "PAYMENT_CARD"

        // Act
        val error = HeadlessError.InvalidTokenizationInputDataError(
            paymentMethodType = paymentMethodType,
            requiredInputData = PrimerCardData::class,
            inputData = PrimerOtpData::class
        )

        // Assert
        assertEquals("invalid-raw-type-data", error.errorId)
        assertEquals(
            "PrimerHeadlessUniversalCheckout tokenization error for PAYMENT_CARD and input data PrimerOtpData",
            error.description
        )
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertNull(error.errorCode)
        assertEquals(
            "Make sure you provide data of type PrimerCardData for payment method PAYMENT_CARD.",
            error.recoverySuggestion
        )
        assertSame(error, error.exposedError)

        // Check context
        assertEquals(ErrorContextParams("invalid-raw-type-data", paymentMethodType), error.context)
    }
}
