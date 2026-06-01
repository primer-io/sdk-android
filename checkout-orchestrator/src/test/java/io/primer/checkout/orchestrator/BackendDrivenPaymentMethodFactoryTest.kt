package io.primer.checkout.orchestrator

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BackendDrivenPaymentMethodFactoryTest {
    private lateinit var primerSettings: PrimerSettings

    @BeforeEach
    fun setUp() {
        primerSettings = mockk(relaxed = true)
    }

    @Test
    fun `build returns Backend Driven when payment handling is AUTO mode`() {
        every { primerSettings.paymentHandling } returns PrimerPaymentHandling.AUTO
        val factory = BackendDrivenPaymentMethodFactory(type = PAYMENT_METHOD_TYPE, settings = primerSettings)

        // Build BackendDrivenP
        val result = factory.build()

        // Assert result
        assertEquals(true, result is Success)
        assertEquals(true, (result as Success).value is BackendDrivenPaymentMethod)
    }

    @Test
    fun `build returns Failure when payment handling is MANUAL mode`() {
        every { primerSettings.paymentHandling } returns PrimerPaymentHandling.MANUAL

        val factory = BackendDrivenPaymentMethodFactory(type = PAYMENT_METHOD_TYPE, settings = primerSettings)
        // Build BackendDrivenP
        val result = factory.build()

        // Assert result
        assertEquals(true, result is Failure)
    }

    private companion object {

        const val PAYMENT_METHOD_TYPE = "PAYMENT_METHOD"
    }
}
