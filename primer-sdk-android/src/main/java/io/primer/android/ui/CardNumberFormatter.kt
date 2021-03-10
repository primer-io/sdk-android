package io.primer.android.ui

import io.primer.android.logging.Logger
import kotlin.math.max

private val INVALID_CHARACTER = Regex("[^0-9]")
private val ENDS_WITH_WHITESPACE = Regex(".+\\s$")

internal class CardNumberFormatter private constructor(
  private val value: String,
  private val autoInsert: Boolean,
) {

    private val meta = CardType.lookup(value)
    private val log = Logger("card-nummber")

    override fun toString(): String {
        val max = meta.lengths.maxOrNull() ?: 16

        val formatted = buildString {
            value.forEachIndexed { index, c ->
                if (index < max) {
                    append(c)
                    if (meta.gaps.contains(index + 1)) {
                        append(' ')
                    }
                }
            }
        }

        return if (!autoInsert && formatted.endsWith(' ')) {
            formatted.trim()
        } else {
            formatted
        }
    }

    fun getValue(): String {
        return value.replace(INVALID_CHARACTER, "")
    }

    fun isEmpty(): Boolean {
        return getValue().isEmpty()
    }

    fun isValid(): Boolean {
        if (isEmpty()) {
            return false
        }

        if (meta.lengths.contains(value.length).not()) {
            return false
        }

        return isLuhnValid()
    }

    fun getCVVLength(): Int {
        return meta.cvvLength
    }

    private fun isLuhnValid(): Boolean {
        val digits = value.substring(0, value.lastIndex)
        var checksum = value.substring(value.lastIndex).toInt()

        digits.forEachIndexed { n, c ->
            var digit = Character.getNumericValue(c)

            if ((n % 2) == 0) {
                digit *= 2
            }

            if (digit > 9) {
                digit -= 9
            }

            checksum += digit
        }

        return (checksum % 10) == 0
    }

    companion object {

        fun fromString(str: String, autoInsert: Boolean = false): CardNumberFormatter {
            return CardNumberFormatter(str.replace(INVALID_CHARACTER, ""), autoInsert)
        }
    }
}
