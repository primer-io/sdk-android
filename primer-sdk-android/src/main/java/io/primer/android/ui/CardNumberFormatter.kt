package io.primer.android.ui

private val INVALID_CHARACTER = Regex("[^0-9]")

private const val LUHN_CONSTANT_A: Int = 2
private const val LUHN_CONSTANT_B: Int = 9
private const val LUHN_CONSTANT_C: Int = 10

internal class CardNumberFormatter private constructor(
    private val value: String,
    private val autoInsert: Boolean,
) {

    private val meta = CardType.lookup(value)

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

    fun isValid(): Boolean =
        when {
            isEmpty() -> false
            meta.lengths.none { it == value.length } -> false
            else -> isLuhnValid()
        }

    fun getCvvLength(): Int = meta.cvvLength

    private fun isLuhnValid(): Boolean {
        val digits = value.substring(0, value.lastIndex)
        var checksum = value.substring(value.lastIndex).toInt()

        digits.forEachIndexed { n, c ->
            var digit = Character.getNumericValue(c)

            if ((n % LUHN_CONSTANT_A) == 0) {
                digit *= LUHN_CONSTANT_A
            }

            if (digit > LUHN_CONSTANT_B) {
                digit -= LUHN_CONSTANT_B
            }

            checksum += digit
        }

        return (checksum % LUHN_CONSTANT_C) == 0
    }

    companion object {

        fun fromString(str: String, autoInsert: Boolean = false): CardNumberFormatter {
            return CardNumberFormatter(str.replace(INVALID_CHARACTER, ""), autoInsert)
        }
    }
}
