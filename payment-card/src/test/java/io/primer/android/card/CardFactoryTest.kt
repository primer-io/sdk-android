package io.primer.android.card

import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CardFactoryTest {

    @Test
    fun `build should return Success with PayPal instance`() {
        // Given
        val factory = CardFactory()

        // When
        val result = factory.build()

        // Then
        assertTrue(result is Success<PaymentMethod, Exception>)
        val successResult = result as Success<PaymentMethod, Exception>
        assertTrue(successResult.value is Card)
    }
}
