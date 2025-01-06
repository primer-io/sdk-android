package io.primer.android.nolpay.implementation.errors.data.mapper

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.nolpay.implementation.errors.domain.model.NolPayError
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.nolpay.api.exceptions.NolPaySdkException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NolPayErrorMapperTest {
    private val errorMapper = NolPayErrorMapper()

    @Test
    fun `should emit NolPayError when throwable is NolPaySdkException`() {
        val code = "code"
        val message = "message"
        val throwable = NolPaySdkException(code, message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Nol SDK encountered an error $code. $message"
        val expectedContext =
            ErrorContextParams(
                "nol-pay-sdk-error",
                PaymentMethodType.NOL_PAY.name,
            )

        assertTrue(actualResult is NolPayError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `should throw unsupported mapping exception when throwable is not known mapped Exception`() {
        // Given
        val exception = IllegalStateException("Some error")

        // When / Then
        val thrown =
            assertThrows(IllegalStateException::class.java) {
                errorMapper.getPrimerError(exception)
            }
        assertEquals(
            "Unsupported mapping for java.lang.IllegalStateException:" +
                " Some error in io.primer.android.nolpay.implementation.errors.data.mapper.NolPayErrorMapper",
            thrown.message,
        )
    }
}
