package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.NolPayError
import io.primer.nolpay.api.exceptions.NolPaySdkException
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
        val expectedContext = ErrorContextParams(
            "nol-pay-sdk-error",
            PaymentMethodType.NOL_PAY.name
        )

        assertTrue(actualResult is NolPayError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `should emit GeneralError when throwable is not known mapped Exception`() {
        val message = "message"
        val throwable = Exception(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Something went wrong. Message $message."

        assertTrue(actualResult is GeneralError.UnknownError)
        assertEquals(expectedDescription, actualResult.description)
    }
}
