package io.primer.android.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Currency

internal class PaymentUtilsTest {

    @Test
    fun `minorToAmount should format the amount to the correct number of decimal places`() {
        Assertions.assertEquals(
            1.000,
            PaymentUtils.minorToAmount(1000, 3)
        )
    }

    @Test
    fun `minorToAmount should format the amount to the correct number of decimal places for currency`() {
        val currency = Currency.getInstance("EUR")
        Assertions.assertEquals(
            10.00,
            PaymentUtils.minorToAmount(1000, currency)
        )
    }
}
