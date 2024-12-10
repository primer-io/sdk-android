import io.mockk.every
import io.mockk.mockk
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.configuration.data.exception.MissingConfigurationException
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.data.network.exception.InvalidUrlException
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.exception.JsonEncodingException
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.errors.data.mapper.DefaultErrorMapper
import io.primer.android.errors.domain.models.ClientError
import io.primer.android.errors.domain.models.ConnectivityError
import io.primer.android.errors.domain.models.GeneralError
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.errors.domain.models.ParserError
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.errors.domain.models.SessionCreateError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertIs

class DefaultErrorMapperTest {

    private val errorMapper = DefaultErrorMapper()

    @Test
    fun `getPrimerError should return ConnectivityError when IOException is thrown`() {
        val ioException = IOException("Network error")
        val result = errorMapper.getPrimerError(ioException)
        assertTrue(result is ConnectivityError)
        assertEquals("Network error", (result as ConnectivityError).description)
    }

    @Test
    fun `getPrimerError should return ParserError_EncodeError when JsonEncodingException is thrown`() {
        val exception = Exception("Encoding error")
        val jsonException = JsonEncodingException(exception)
        val result = errorMapper.getPrimerError(jsonException)
        assertTrue(result is ParserError.EncodeError)
        assertEquals("Failed to encode $exception", (result as ParserError.EncodeError).description)
    }

    @Test
    fun `getPrimerError should return ParserError_DecodeError when JsonDecodingException is thrown`() {
        val exception = Exception("Decoding error")
        val jsonException = JsonDecodingException(exception)
        val result = errorMapper.getPrimerError(jsonException)
        assertTrue(result is ParserError.DecodeError)
        assertEquals("Failed to decode $exception", (result as ParserError.DecodeError).description)
    }

    @Test
    fun `getPrimerError should return HttpUnauthorizedError when HttpException with unauthorized error is thrown`() {
        val httpException = mockk<HttpException> {
            every { isUnAuthorizedError() } returns true
            every { errorCode } returns 401
            every { error } returns mockk {
                every { diagnosticsId } returns "diag-123"
            }
        }
        val result = errorMapper.getPrimerError(httpException)
        assertTrue(result is HttpError.HttpUnauthorizedError)
        assertEquals("401", (result as HttpError.HttpUnauthorizedError).errorCode)
        assertEquals("diag-123", result.diagnosticsId)
    }

    @Test
    fun `getPrimerError should return HttpServerError when HttpException with server error is thrown`() {
        val httpException = mockk<HttpException>(relaxed = true) {
            every { isServerError() } returns true
            every { errorCode } returns 500
            every { error } returns mockk {
                every { diagnosticsId } returns "diag-500"
                every { description } returns "Internal Server Error"
            }
        }
        val result = errorMapper.getPrimerError(httpException)
        assertTrue(result is HttpError.HttpServerError)
        assertEquals("500", (result as HttpError.HttpServerError).errorCode)
        assertEquals("diag-500", result.diagnosticsId)
        assertEquals("Server error [500] Response: Internal Server Error", result.description)
    }

    @Test
    fun `getPrimerError should return HttpClientError when HttpException with client error is thrown`() {
        val httpException = mockk<HttpException>(relaxed = true) {
            every { isClientError() } returns true
            every { errorCode } returns 400
            every { error } returns mockk {
                every { diagnosticsId } returns "diag-400"
                every { description } returns "Bad Request"
            }
        }
        val result = errorMapper.getPrimerError(httpException)
        assertTrue(result is HttpError.HttpClientError)
        assertEquals("400", (result as HttpError.HttpClientError).errorCode)
        assertEquals("diag-400", result.diagnosticsId)
        assertEquals("Server error [400] Response: Bad Request", result.description)
        assertEquals(ClientError("Bad Request", "diag-400"), result.exposedError)
    }

    @Test
    fun `getPrimerError should return PaymentMethodCancelledError when PaymentMethodCancelledException is thrown`() {
        val exception = PaymentMethodCancelledException("BANK")
        val result = errorMapper.getPrimerError(exception)
        assertTrue(result is PaymentMethodCancelledError)
        assertEquals("BANK", (result as PaymentMethodCancelledError).paymentMethodType)
    }

    @Test
    fun `getPrimerError should return MissingConfigurationError when MissingConfigurationException is thrown`() {
        val exception = MissingConfigurationException(Throwable("Missing configuration"))
        val result = errorMapper.getPrimerError(exception)
        assertTrue(result is GeneralError.MissingConfigurationError)
    }

    @Test
    fun `getPrimerError should return InvalidValueError when IllegalValueException is thrown`() {
        val exception = IllegalValueException(
            mockk { every { key } returns "key" },
            "Value is invalid"
        )
        val result = errorMapper.getPrimerError(exception)
        assertTrue(result is GeneralError.InvalidValueError)
        assertEquals("key", (result as GeneralError.InvalidValueError).illegalValueKey.key)
        assertEquals("Value is invalid", result.message)
    }

    @Test
    fun `getPrimerError should return InvalidUrlError when InvalidUrlException is thrown`() {
        val exception = InvalidUrlException("Invalid URL")
        val result = errorMapper.getPrimerError(exception)
        assertTrue(result is GeneralError.InvalidUrlError)
    }

    @Test
    fun `getPrimerError should throw IllegalArgumentException when unsupported exception is thrown`() {
        val exception = IllegalArgumentException("Unsupported exception")
        val thrownException = assertThrows<IllegalArgumentException> {
            errorMapper.getPrimerError(exception)
        }
        assertEquals("Unsupported mapping for $exception", thrownException.message)
    }

    @Test
    fun `getPrimerError should return SessionCreateError when SessionCreateException is thrown`() {
        val exception = SessionCreateException(
            paymentMethodType = "KLARNA",
            diagnosticsId = "diagnosticsId",
            description = "description"
        )
        val result = errorMapper.getPrimerError(exception)
        assertTrue(result is SessionCreateError)
        assertEquals("failed-to-create-session", result.errorId)
        assertEquals("Failed to create session for KLARNA. description", result.description)
        assertNull(result.errorCode)
        assertEquals("diagnosticsId", result.diagnosticsId)
        assertEquals(
            "Ensure that the KLARNA is configured correctly on the dashboard (https://dashboard.primer.io/)",
            result.recoverySuggestion
        )
        assertIs<ErrorContextParams>(result.context)
    }
}
