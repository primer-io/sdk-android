package io.primer.android.internal.extensions

/** ASCII code point for uppercase 'A'. */
private const val ASCII_UPPERCASE_A = 0x41

/** Unicode code point for Regional Indicator Symbol Letter A. */
private const val REGIONAL_INDICATOR_A = 0x1F1E6

/**
 * Converts a 2-letter country code to a flag emoji.
 * Uses Unicode regional indicator symbols.
 */
internal fun String.toFlagEmoji(): String {
    if (length != 2) return ""
    val firstChar = Character.codePointAt(uppercase(), 0) - ASCII_UPPERCASE_A + REGIONAL_INDICATOR_A
    val secondChar = Character.codePointAt(uppercase(), 1) - ASCII_UPPERCASE_A + REGIONAL_INDICATOR_A
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}
