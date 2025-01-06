package io.primer.android.ipay88

import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IPay88FactoryTest {
    @Test
    fun `build should return Success with iPay88 instance`() {
        // Given
        val type = "ipay88Type"
        val factory = IPay88Factory(type)

        // When
        val result = factory.build()

        // Then
        assertTrue(result is Success<PaymentMethod, Exception>)
        val successResult = result as Success<PaymentMethod, Exception>
        assertTrue(successResult.value is IPay88PaymentMethod)
        val paymentMethod = successResult.value as IPay88PaymentMethod
        assertTrue(paymentMethod.type == type)
    }
}
