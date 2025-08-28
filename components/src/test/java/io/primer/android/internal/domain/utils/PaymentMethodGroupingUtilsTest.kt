package io.primer.android.internal.domain.utils

import io.primer.android.configuration.domain.model.Surcharge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentMethodGroupingUtilsTest {

    @Test
    fun `getValue returns 0 when surcharge is null`() {
        val result = null.getValue()
        assertEquals(0, result)
    }

    @Test
    fun `getValue returns amount for PaymentMethodSurcharge`() {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = 250)
        val result = surcharge.getValue()
        assertEquals(250, result)
    }

    @Test
    fun `getValue returns 0 for PaymentMethodSurcharge with amount 0`() {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = 0)
        val result = surcharge.getValue()
        assertEquals(0, result)
    }

    @Test
    fun `getValue returns negative amount for PaymentMethodSurcharge with negative amount`() {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = -100)
        val result = surcharge.getValue()
        assertEquals(-100, result)
    }

    @Test
    fun `getValue returns UNKNOWN_SURCHARGE for CardNetworksSurcharge with non-zero surcharges`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(
                "visa" to 100,
                "mastercard" to 0,
            ),
        )
        val result = surcharge.getValue()
        assertEquals(UNKNOWN_SURCHARGE, result)
    }

    @Test
    fun `getValue returns UNKNOWN_SURCHARGE for CardNetworksSurcharge with all non-zero surcharges`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(
                "visa" to 100,
                "mastercard" to 200,
                "amex" to 150,
            ),
        )
        val result = surcharge.getValue()
        assertEquals(UNKNOWN_SURCHARGE, result)
    }

    @Test
    fun `getValue returns 0 for CardNetworksSurcharge with all zero surcharges`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(
                "visa" to 0,
                "mastercard" to 0,
                "amex" to 0,
            ),
        )
        val result = surcharge.getValue()
        assertEquals(0, result)
    }

    @Test
    fun `getValue returns 0 for CardNetworksSurcharge with empty surcharges map`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = emptyMap(),
        )
        val result = surcharge.getValue()
        assertEquals(0, result)
    }

    @Test
    fun `getValue returns UNKNOWN_SURCHARGE for CardNetworksSurcharge with single non-zero surcharge`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf("visa" to 50),
        )
        val result = surcharge.getValue()
        assertEquals(UNKNOWN_SURCHARGE, result)
    }

    @Test
    fun `getValue returns UNKNOWN_SURCHARGE for CardNetworksSurcharge with negative surcharge values`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(
                "visa" to -50,
                "mastercard" to 0,
            ),
        )
        val result = surcharge.getValue()
        assertEquals(UNKNOWN_SURCHARGE, result)
    }

    @Test
    fun `UNKNOWN_SURCHARGE constant has expected value`() {
        assertEquals(100000, UNKNOWN_SURCHARGE)
    }
}
