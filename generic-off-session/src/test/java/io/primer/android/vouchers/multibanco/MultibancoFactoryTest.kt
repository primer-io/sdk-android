package io.primer.android.vouchers.multibanco

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MultibancoFactoryTest {

    @Test
    fun `build should return Success with Multibanco PaymentMethod`() {
        val paymentMethodType = "multibanco"
        val bankIssuerFactory = MultibancoFactory(paymentMethodType)

        val result: Either<PaymentMethod, Exception> = bankIssuerFactory.build()

        assertTrue(result is Success)
        if (result is Success) {
            val paymentMethod = result.value
            assertTrue(paymentMethod is Multibanco)
            assertTrue((paymentMethod as Multibanco).paymentMethodType == paymentMethodType)
        }
    }
}
