package io.primer.android.payments.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Currency
import kotlin.math.pow

class PaymentUtilsTest {
    @Test
    fun `minorToAmount with currency should return correct amount`() {
        // Arrange
        val minorAmount = 1234
        val currency = Currency.getInstance("USD") // USD has 2 fraction digits

        // Act
        val result = PaymentUtils.minorToAmount(minorAmount, currency)

        // Assert
        val expected = minorAmount.toDouble() / 10.0.pow(currency.defaultFractionDigits)
        assertEquals(expected, result)
    }

    @Test
    fun `minorToAmount with fraction digits should return correct amount`() {
        // Arrange
        val minorAmount = 1234
        val fractionDigits = 2

        // Act
        val result = PaymentUtils.minorToAmount(minorAmount, fractionDigits)

        // Assert
        val expected = minorAmount.toDouble() / 10.0.pow(fractionDigits)
        assertEquals(expected, result)
    }

    @Test
    fun `minorToAmount with zero fraction digits should return correct amount`() {
        // Arrange
        val minorAmount = 1234
        val fractionDigits = 0

        // Act
        val result = PaymentUtils.minorToAmount(minorAmount, fractionDigits)

        // Assert
        val expected = minorAmount.toDouble() / 10.0.pow(fractionDigits)
        assertEquals(expected, result)
    }

    @Test
    fun `minorToAmount with currency having zero fraction digits should return correct amount`() {
        // Arrange
        val minorAmount = 1234
        val currency = Currency.getInstance("JPY") // JPY has 0 fraction digits

        // Act
        val result = PaymentUtils.minorToAmount(minorAmount, currency)

        // Assert
        val expected = minorAmount.toDouble() / 10.0.pow(currency.defaultFractionDigits)
        assertEquals(expected, result)
    }

    @Test
    fun `minorToAmount with currency having three fraction digits should return correct amount`() {
        // Arrange
        val minorAmount = 1234
        val currency = Currency.getInstance("KWD") // KWD has 3 fraction digits

        // Act
        val result = PaymentUtils.minorToAmount(minorAmount, currency)

        // Assert
        val expected = minorAmount.toDouble() / 10.0.pow(currency.defaultFractionDigits)
        assertEquals(expected, result)
    }
}
