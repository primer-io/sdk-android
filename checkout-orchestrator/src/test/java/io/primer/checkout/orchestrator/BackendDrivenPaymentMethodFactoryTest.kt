package io.primer.checkout.orchestrator

import io.primer.android.core.utils.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BackendDrivenPaymentMethodFactoryTest {

    @Test
    fun `build returns Backend Driven, leaving its availability to its checker`() {
        val result = BackendDrivenPaymentMethodFactory(type = PAYMENT_METHOD_TYPE).build()

        assertEquals(true, result is Success)
        assertEquals(true, (result as Success).value is BackendDrivenPaymentMethod)
    }

    private companion object {

        const val PAYMENT_METHOD_TYPE = "PAYMENT_METHOD"
    }
}
