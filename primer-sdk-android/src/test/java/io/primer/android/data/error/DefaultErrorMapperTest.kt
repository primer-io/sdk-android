package io.primer.android.data.error

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.domain.error.models.PaymentMethodError
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultErrorMapperTest {
    private val errorMapper = DefaultErrorMapper()

    @Test
    fun `should emit PaymentMethodCancelledError when throwable is PaymentMethodCancelledException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val throwable = PaymentMethodCancelledException(paymentMethodType.name)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Vaulting/Checking out for $paymentMethodType was cancelled by the user."
        val expectedContext = ErrorContextParams(
            "payment-cancelled",
            paymentMethodType.name
        )

        assertTrue(actualResult is PaymentMethodError.PaymentMethodCancelledError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }
}
