package io.primer.android.utils

import io.primer.android.model.dto.MonetaryAmount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.text.DecimalFormatSymbols

class PaymentUtilsTest {

    @Test
    fun `amountToCurrencyString should format USD correctly`() {
        val monetaryAmount = MonetaryAmount.create("USD", AMOUNT)
        assertEquals(
            "$1${decimalSeparator}00", PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format GBP correctly`() {
        val monetaryAmount = MonetaryAmount.create("GBP", AMOUNT)
        assertEquals("£1${decimalSeparator}00", PaymentUtils.amountToCurrencyString(monetaryAmount))
    }

    @Test
    fun `amountToCurrencyString should format EUR correctly`() {
        val monetaryAmount = MonetaryAmount.create("EUR", AMOUNT)
        assertEquals("€1${decimalSeparator}00", PaymentUtils.amountToCurrencyString(monetaryAmount))
    }

    @Test
    fun `amountToCurrencyString should format JPY correctly`() {
        val monetaryAmount = MonetaryAmount.create("JPY", AMOUNT)
        assertEquals("¥100", PaymentUtils.amountToCurrencyString(monetaryAmount))
    }

    @Test
    fun `amountToCurrencyString should format KRW correctly`() {
        val monetaryAmount = MonetaryAmount.create("KRW", AMOUNT)
        assertEquals("₩100", PaymentUtils.amountToCurrencyString(monetaryAmount))
    }

    @Test
    fun `amountToCurrencyString should format SEK correctly`() {
        val monetaryAmount = MonetaryAmount.create("SEK", AMOUNT)
        assertEquals(
            "SEK1${decimalSeparator}00",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format CNY correctly`() {
        val monetaryAmount = MonetaryAmount.create("CNY", AMOUNT)
        assertEquals(
            "CN¥1${decimalSeparator}00",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format huge USD correctly`() {
        val monetaryAmount = MonetaryAmount.create("USD", 999999999)
        assertEquals(
            "$9${groupingSeparator}999${groupingSeparator}999${decimalSeparator}99",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format huge JPY correctly`() {
        val monetaryAmount = MonetaryAmount.create("JPY", 999999999)
        assertEquals(
            "¥999${groupingSeparator}999${groupingSeparator}999",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format small USD correctly`() {
        val monetaryAmount = MonetaryAmount.create("USD", 1)
        assertEquals(
            "$0${decimalSeparator}01",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    @Test
    fun `amountToCurrencyString should format small JPY correctly`() {
        val monetaryAmount = MonetaryAmount.create("JPY", 1)
        assertEquals(
            "¥1",
            PaymentUtils.amountToCurrencyString(monetaryAmount)
        )
    }

    private companion object {
        const val AMOUNT = 100
        val groupingSeparator = DecimalFormatSymbols.getInstance().groupingSeparator
        val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    }
}
