package io.primer.android.vouchers.retailOutlets

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RetailOutletsFactoryTest {

    @Test
    fun `build should return Success with RetailOutlets PaymentMethod`() {
        val paymentMethodType = "retailOutlets"
        val bankIssuerFactory = RetailOutletsFactory(paymentMethodType)

        val result: Either<PaymentMethod, Exception> = bankIssuerFactory.build()

        assertTrue(result is Success)
        if (result is Success) {
            val paymentMethod = result.value
            assertTrue(paymentMethod is RetailOutlets)
            assertTrue((paymentMethod as RetailOutlets).paymentMethodType == paymentMethodType)
        }
    }
}
