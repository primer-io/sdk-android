package io.primer.android.ui

private val INVALID_CHARACTER = Regex("[^0-9]")

internal class CardNumberFormatter private constructor(
    private val value: String,
    private val autoInsert: Boolean,
) {

    private val meta = CardType.lookup(value)

    override fun toString(): String {
        val max = meta.lengths.maxOrNull() ?: META_DEFAULT_LENGTH

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

    fun getMaxLength() = meta.lengths.maxOf { it }

    fun getCvvLength(): Int = meta.cvvLength

    fun getCardType(): CardType.Type = meta.type

    @Suppress("MagicNumber")
    private fun isLuhnValid(): Boolean {
        var sum = 0
        var alternate = false
        for (i in value.length - 1 downTo 0) {
            var n = value.substring(i, i + 1).toInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = n % 10 + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    companion object {

        const val META_DEFAULT_LENGTH = 16

        fun fromString(str: String, autoInsert: Boolean = false): CardNumberFormatter {
            return CardNumberFormatter(str.replace(INVALID_CHARACTER, ""), autoInsert)
        }
    }
}
