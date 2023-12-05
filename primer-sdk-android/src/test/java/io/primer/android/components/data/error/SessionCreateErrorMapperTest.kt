package io.primer.android.components.data.error

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.SessionCreateError
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionCreateErrorMapperTest {
    private val errorMapper = SessionCreateErrorMapper()

    @Test
    fun `should emit SessionCreateError when throwable is SessionCreateException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val description = "description"
        val throwable =
            SessionCreateException(paymentMethodType, "diagnosticId", description)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Failed to create session for ${paymentMethodType.name}. $description"
        val expectedContext = ErrorContextParams(
            "failed-to-create-session",
            paymentMethodType.name
        )

        assertTrue(actualResult is SessionCreateError)
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
