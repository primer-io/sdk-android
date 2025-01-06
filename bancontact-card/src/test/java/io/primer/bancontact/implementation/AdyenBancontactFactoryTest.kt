package io.primer.bancontact.implementation

import io.primer.android.bancontact.AdyenBancontact
import io.primer.android.bancontact.AdyenBancontactFactory
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdyenBancontactFactoryTest {
    @Test
    fun `build should return Success with PayPal instance`() {
        // Given
        val factory = AdyenBancontactFactory()

        // When
        val result = factory.build()

        // Then
        assertTrue(result is Success<PaymentMethod, Exception>)
        val successResult = result as Success<PaymentMethod, Exception>
        assertTrue(successResult.value is AdyenBancontact)
    }
}
