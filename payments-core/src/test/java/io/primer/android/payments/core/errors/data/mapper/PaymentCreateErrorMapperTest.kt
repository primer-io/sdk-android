package io.primer.android.payments.core.errors.data.mapper

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.payments.core.errors.data.exception.PaymentCreateException
import io.primer.android.payments.core.errors.domain.model.PaymentError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class PaymentCreateErrorMapperTest {
    private val errorMapper = PaymentCreateErrorMapper()

    @Test
    fun `getPrimerError should return HttpClientError for PaymentCreateException with HttpException cause`() {
        // Arrange
        val httpException = mockk<HttpException>()
        val diagnosticsId = "12345"
        val description = "Client error"
        every { httpException.isClientError() } returns true
        every { httpException.errorCode } returns 400
        every { httpException.error.diagnosticsId } returns diagnosticsId
        every { httpException.error.description } returns description

        val paymentCreateException = PaymentCreateException(cause = httpException)

        // Act
        val primerError = errorMapper.getPrimerError(paymentCreateException)

        // Assert
        assertTrue(primerError is HttpError.HttpClientError)
        primerError as HttpError.HttpClientError
        assertEquals("400", primerError.errorCode)
        assertEquals(diagnosticsId, primerError.diagnosticsId)
        assertTrue(primerError.exposedError is PaymentError.PaymentCreateFailedError)
        val paymentCreateFailedError = primerError.exposedError as PaymentError.PaymentCreateFailedError
        assertEquals(description, paymentCreateFailedError.description)
        assertEquals(diagnosticsId, paymentCreateFailedError.diagnosticsId)
    }

    @Test
    fun `getPrimerError should throw IllegalArgumentException for unsupported Throwable`() {
        // Arrange
        val unsupportedException = Exception("Unsupported exception")

        // Act & Assert
        val exception =
            assertFailsWith<IllegalArgumentException> {
                errorMapper.getPrimerError(unsupportedException)
            }
        assertTrue(exception.message!!.contains("Unsupported mapping for"))
    }
}
