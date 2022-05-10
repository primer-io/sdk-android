package io.primer.android.ui

import org.junit.Assert.assertEquals
import org.junit.Test

internal class CardNumberFormatterTest {

    private val cards = listOf(
        "4111 1111 1111 1111",
        "4444 3333 2222 1111455",
        "5555 5555 5555 4444",
        "2223 0031 2200 3222",
        "3782 822463 10005",
        "6011 1111 1111 1117",
        "3056 930902 5904",
        "3569 9900 1003 0400",
        "6759 0000 0000 0000005",
    )

    @Test
    fun `toString renders spacing correctly for main card types`() {
        cards.forEach { c ->
            val cardNumber = CardNumberFormatter.fromString(c)
            assertEquals(c, cardNumber.toString())
        }
    }

    @Test
    fun `all valid card numbers pass validation`() {
        cards.forEach { c ->
            val cardNumber = CardNumberFormatter.fromString(c)
            cardNumber.isValid()
            assertEquals(true, cardNumber.isValid())
        }
    }

    @Test
    fun `too short card number fails validation`() {
        val c = "4111 1111"
        val cardNumber = CardNumberFormatter.fromString(c)
        cardNumber.isValid()
        assertEquals(false, cardNumber.isValid())
    }

    @Test
    fun `non luhn card number fails validation`() {
        val c = "4111 1111 1111 1112"
        val cardNumber = CardNumberFormatter.fromString(c)
        cardNumber.isValid()
        assertEquals(false, cardNumber.isValid())
    }

    @Test
    fun `empty card number fails validation`() {
        val c = ""
        val cardNumber = CardNumberFormatter.fromString(c)
        cardNumber.isValid()
        assertEquals(false, cardNumber.isValid())
    }
}
