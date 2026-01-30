package io.primer.android.internal.presentation.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class NumeralUtilsTest {

    @Nested
    @DisplayName("convertToWesternNumeral")
    inner class ConvertToWesternNumeralTests {

        @ParameterizedTest(name = "Arabic-Indic {0} → Western {1}")
        @CsvSource(
            "٠, 0",
            "١, 1",
            "٢, 2",
            "٣, 3",
            "٤, 4",
            "٥, 5",
            "٦, 6",
            "٧, 7",
            "٨, 8",
            "٩, 9",
        )
        fun `converts Arabic-Indic numerals to Western`(arabicIndic: Char, expected: Char) {
            val result = convertToWesternNumeral(arabicIndic)
            assertEquals(expected, result)
        }

        @ParameterizedTest(name = "Persian {0} → Western {1}")
        @CsvSource(
            "۰, 0",
            "۱, 1",
            "۲, 2",
            "۳, 3",
            "۴, 4",
            "۵, 5",
            "۶, 6",
            "۷, 7",
            "۸, 8",
            "۹, 9",
        )
        fun `converts Persian numerals to Western`(persian: Char, expected: Char) {
            val result = convertToWesternNumeral(persian)
            assertEquals(expected, result)
        }

        @ParameterizedTest(name = "Western {0} → Western {0}")
        @CsvSource(
            "0, 0",
            "1, 1",
            "5, 5",
            "9, 9",
        )
        fun `keeps Western numerals unchanged`(western: Char, expected: Char) {
            val result = convertToWesternNumeral(western)
            assertEquals(expected, result)
        }

        @ParameterizedTest(name = "Non-digit '{0}' → '{0}'")
        @CsvSource(
            "A, A",
            "Z, Z",
            "-, -",
            "., .",
        )
        fun `keeps non-digit characters unchanged`(char: Char, expected: Char) {
            val result = convertToWesternNumeral(char)
            assertEquals(expected, result)
        }

        @Test
        fun `keeps space character unchanged`() {
            val result = convertToWesternNumeral(' ')
            assertEquals(' ', result)
        }
    }

    @Nested
    @DisplayName("String.toWesternNumeralsOnly")
    inner class ToWesternNumeralsOnlyTests {

        @Test
        fun `filters Arabic-Indic string to Western numerals only`() {
            val result = "٠١٢٣٤٥٦٧٨٩".toWesternNumeralsOnly()
            assertEquals("0123456789", result)
        }

        @Test
        fun `filters Persian string to Western numerals only`() {
            val result = "۰۱۲۳۴۵۶۷۸۹".toWesternNumeralsOnly()
            assertEquals("0123456789", result)
        }

        @Test
        fun `filters mixed Western and Arabic-Indic string`() {
            val result = "5٩2٨1١".toWesternNumeralsOnly()
            assertEquals("592811", result)
        }

        @Test
        fun `filters mixed Western and Persian string`() {
            val result = "4۴3۳2۲".toWesternNumeralsOnly()
            assertEquals("443322", result)
        }

        @Test
        fun `filters string with letters and numbers to numbers only`() {
            val result = "AB١CD۲EF3".toWesternNumeralsOnly()
            assertEquals("123", result)
        }

        @Test
        fun `returns empty string for empty input`() {
            val result = "".toWesternNumeralsOnly()
            assertEquals("", result)
        }

        @Test
        fun `returns empty string for string with only letters`() {
            val result = "ABCDEF".toWesternNumeralsOnly()
            assertEquals("", result)
        }

        @Test
        fun `filters card number with mixed Arabic-Indic and spaces`() {
            val result = "٤٢٢٢ ١١١١ ١١١١ 1111".toWesternNumeralsOnly()
            assertEquals("4222111111111111", result)
        }

        @Test
        fun `filters card number with mixed Persian and spaces`() {
            val result = "۵۱۱۱ ۲۲۲۲ ۳۳۳۳ 4444".toWesternNumeralsOnly()
            assertEquals("5111222233334444", result)
        }

        @Test
        fun `filters CVV with Arabic-Indic numerals`() {
            val result = "١٢٣".toWesternNumeralsOnly()
            assertEquals("123", result)
        }

        @Test
        fun `filters CVV with Persian numerals`() {
            val result = "۴۵۶".toWesternNumeralsOnly()
            assertEquals("456", result)
        }

        @Test
        fun `filters mixed script with special characters`() {
            val result = "٤-٢-٢-٢".toWesternNumeralsOnly()
            assertEquals("4222", result)
        }

        @Test
        fun `handles string with only spaces`() {
            val result = "   ".toWesternNumeralsOnly()
            assertEquals("", result)
        }

        @Test
        fun `handles string with only special characters`() {
            val result = "!@#$%^&*()".toWesternNumeralsOnly()
            assertEquals("", result)
        }
    }
}
