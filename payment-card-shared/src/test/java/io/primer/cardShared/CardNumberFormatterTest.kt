package io.primer.cardShared

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.extension.sanitizedCardNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardNumberFormatterTest {
    @BeforeEach
    fun setUp() {
        mockkObject(CardNetwork)
        mockkStatic("io.primer.android.configuration.extension.StringExtKt")
    }

    @Test
    fun `toString formats card number correctly with autoInsert true`() {
        val cardNumber = "4111111111111111"
        every { cardNumber.sanitizedCardNumber() } returns cardNumber

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        val formattedNumber = formatter.toString()

        assertEquals("4111 1111 1111 1111", formattedNumber)
    }

    @Test
    fun `toString formats card number correctly with autoInsert false`() {
        val cardNumber = "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = false)
        val formattedNumber = formatter.toString()

        assertEquals("4111 1111 1111 1111", formattedNumber)
    }

    @Test
    fun `isValid returns true for valid card number`() {
        val cardNumber = "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertTrue(formatter.isValid())
    }

    @Test
    fun `isValid returns false for invalid card number`() {
        val cardNumber = "4111111111111112"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertFalse(formatter.isValid())
    }

    @Test
    fun `getValue returns sanitized card number`() {
        val cardNumber = "4111 1111 1111 1111"
        every { cardNumber.sanitizedCardNumber() } returns "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertEquals("4111111111111111", formatter.getValue())
    }

    @Test
    fun `isEmpty returns true for empty card number`() {
        val cardNumber = ""
        every { cardNumber.sanitizedCardNumber() } returns ""

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertTrue(formatter.isEmpty())
    }

    @Test
    fun `isEmpty returns false for non-empty card number`() {
        val cardNumber = "4111111111111111"
        every { cardNumber.sanitizedCardNumber() } returns cardNumber

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `getMaxLength returns the maximum length of the card number`() {
        val cardNumber = "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertEquals(19, formatter.getMaxLength())
    }

    @Test
    fun `getCvvLength returns the CVV length of the card`() {
        val cardNumber = "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertEquals(3, formatter.getCvvLength())
    }

    @Test
    fun `getCardType returns the type of the card`() {
        val cardNumber = "4111111111111111"

        val formatter = CardNumberFormatter.fromString(cardNumber, autoInsert = true)
        assertEquals(CardNetwork.Type.VISA, formatter.getCardType())
    }
}
