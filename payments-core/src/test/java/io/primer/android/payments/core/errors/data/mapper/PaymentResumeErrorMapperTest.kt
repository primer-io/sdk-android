package io.primer.android.payments.core.errors.data.mapper

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.payments.core.errors.data.exception.PaymentResumeException
import io.primer.android.payments.core.errors.domain.model.PaymentError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class PaymentResumeErrorMapperTest {

    private val errorMapper = PaymentResumeErrorMapper()

    @Test
    fun `getPrimerError should return HttpClientError for PaymentResumeException with HttpException cause`() {
        // Arrange
        val httpException = mockk<HttpException>()
        val diagnosticsId = "12345"
        val description = "Client error"
        every { httpException.isClientError() } returns true
        every { httpException.errorCode } returns 400
        every { httpException.error.diagnosticsId } returns diagnosticsId
        every { httpException.error.description } returns description

        val paymentResumeException = PaymentResumeException(cause = httpException)

        // Act
        val primerError = errorMapper.getPrimerError(paymentResumeException)

        // Assert
        assertTrue(primerError is HttpError.HttpClientError)
        primerError as HttpError.HttpClientError
        assertEquals("400", primerError.errorCode)
        assertEquals(diagnosticsId, primerError.diagnosticsId)
        assertTrue(primerError.exposedError is PaymentError.PaymentResumeFailedError)
        val paymentResumeFailedError = primerError.exposedError as PaymentError.PaymentResumeFailedError
        assertEquals(description, paymentResumeFailedError.description)
        assertEquals(diagnosticsId, paymentResumeFailedError.diagnosticsId)
    }

    @Test
    fun `getPrimerError should throw IllegalArgumentException for unsupported Throwable`() {
        // Arrange
        val unsupportedException = Exception("Unsupported exception")

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            errorMapper.getPrimerError(unsupportedException)
        }
        assertTrue(exception.message!!.contains("Unsupported mapping for"))
    }
}
