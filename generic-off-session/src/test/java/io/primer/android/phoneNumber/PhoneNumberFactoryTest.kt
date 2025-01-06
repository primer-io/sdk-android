package io.primer.android.phoneNumber

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhoneNumberFactoryTest {
    @Test
    fun `build should return Success with PhoneNumber PaymentMethod`() {
        val paymentMethodType = "phoneNumber"
        val bankIssuerFactory = PhoneNumberFactory(paymentMethodType)

        val result: Either<PaymentMethod, Exception> = bankIssuerFactory.build()

        assertTrue(result is Success)
        if (result is Success) {
            val paymentMethod = result.value
            assertTrue(paymentMethod is PhoneNumber)
            assertTrue((paymentMethod as PhoneNumber).paymentMethodType == paymentMethodType)
        }
    }
}
