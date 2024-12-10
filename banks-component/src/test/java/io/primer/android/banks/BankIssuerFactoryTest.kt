package io.primer.android.banks

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BankIssuerFactoryTest {

    @Test
    fun `build should return Success with BankIssuerPaymentMethod`() {
        val paymentMethodType = "BANK"
        val bankIssuerFactory = BankIssuerFactory(paymentMethodType)

        val result: Either<PaymentMethod, Exception> = bankIssuerFactory.build()

        assertTrue(result is Success)
        if (result is Success) {
            val paymentMethod = result.value
            assertTrue(paymentMethod is BankIssuerPaymentMethod)
            assertTrue((paymentMethod as BankIssuerPaymentMethod).paymentMethodType == paymentMethodType)
        }
    }
}
