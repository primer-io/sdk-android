package io.primer.android.paypal

import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayPalFactoryTest {
    @Test
    fun `build should return Success with PayPal instance`() {
        // Given
        val type = "PayPalType"
        val factory = PayPalFactory(type)

        // When
        val result = factory.build()

        // Then
        assertTrue(result is Success<PaymentMethod, Exception>)
        val successResult = result as Success<PaymentMethod, Exception>
        assertTrue(successResult.value is PayPal)
        val payPal = successResult.value as PayPal
        assertTrue(payPal.type == type)
    }
}
