package io.primer.android.internal.presentation.utils

/**
 * Converts Arabic-Indic and other numeral systems to Western Arabic numerals (0-9).
 * Supports: Arabic-Indic (٠-٩), Persian/Farsi (۰-۹), and other Unicode digit variants.
 *
 * This is essential for RTL languages where keyboards display locale-specific numerals
 * but card systems require Western numerals.
 */
internal fun convertToWesternNumeral(char: Char): Char {
    return when (char) {
        in '٠'..'٩' -> '0' + (char - '٠') // Arabic-Indic numerals
        in '۰'..'۹' -> '0' + (char - '۰') // Persian/Farsi numerals
        else -> if (char.isDigit()) Character.getNumericValue(char).toString()[0] else char
    }
}

/**
 * Filters and converts input string to Western numerals only.
 * Useful for card number, CVV, and other numeric payment fields in RTL locales.
 */
internal fun String.toWesternNumeralsOnly(): String {
    return this.map { convertToWesternNumeral(it) }
        .filter { it in '0'..'9' }
        .joinToString("")
}
