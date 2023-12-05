package io.primer.android.data.payments.methods.error

import io.mockk.every
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentMethodsErrorMapperTest {
    private val errorMapper = PaymentMethodsErrorMapper()

    @Test
    fun `should emit MisConfiguredPaymentMethodError when throwable is MissingPaymentMethodException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val throwable = MissingPaymentMethodException(paymentMethodType.name)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Cannot present $paymentMethodType because it has not been configured correctly."
        val expectedContext = ErrorContextParams(
            "misconfigured-payment-method",
            paymentMethodType.name
        )

        assertTrue(actualResult is PaymentMethodError.MisConfiguredPaymentMethodError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `should emit UnsupportedIntentPaymentMethodError when throwable is UnsupportedPaymentIntentException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val intent = mockk<PrimerSessionIntent> {
            every { this@mockk.toString() } returns "intent"
        }
        val throwable = UnsupportedPaymentIntentException(paymentMethodType.name, intent)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Cannot initialize the SDK because $paymentMethodType does not support $intent."
        val expectedContext = ErrorContextParams(
            "unsupported-session-intent",
            paymentMethodType.name
        )

        assertTrue(actualResult is PaymentMethodError.UnsupportedIntentPaymentMethodError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `should emit UnsupportedPaymentMethodError when throwable is UnsupportedPaymentMethodException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val throwable = UnsupportedPaymentMethodException(paymentMethodType.name)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Cannot present $paymentMethodType because it is not supported."
        val expectedContext = ErrorContextParams(
            "unsupported-payment-method-type",
            paymentMethodType.name
        )

        assertTrue(actualResult is PaymentMethodError.UnsupportedPaymentMethodError)
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
