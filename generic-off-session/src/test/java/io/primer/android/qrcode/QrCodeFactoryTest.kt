package io.primer.android.qrcode

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QrCodeFactoryTest {
    @Test
    fun `build should return Success with QrCode PaymentMethod`() {
        val paymentMethodType = "qrCode"
        val bankIssuerFactory = QrCodeFactory(paymentMethodType)

        val result: Either<PaymentMethod, Exception> = bankIssuerFactory.build()

        assertTrue(result is Success)
        if (result is Success) {
            val paymentMethod = result.value
            assertTrue(paymentMethod is QrCode)
            assertTrue((paymentMethod as QrCode).paymentMethodType == paymentMethodType)
        }
    }
}
