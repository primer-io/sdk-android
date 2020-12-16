package io.primer.android.ui

import io.primer.android.logging.Logger

private val INVALID_CHARACTER = Regex("[^0-9]")

internal class CardNumber private constructor(private val value: String) {
  private val meta = CardType.lookup(value)
  private val log = Logger("card-nummber")

  override fun toString(): String {
    return buildString {
      value.forEachIndexed { index, c ->
        append(c)
        if (meta.gaps.contains(index + 1)) {
          append(' ')
        }
      }
    }
  }

  fun getValue() : String {
    return value.replace(INVALID_CHARACTER, "")
  }

  fun isEmpty() : Boolean {
    return getValue().isEmpty()
  }

  fun isValid() : Boolean {
    if (isEmpty()) {
      return false
    }

    if (meta.lengths.contains(value.length).not()) {
      return false
    }

    return isLuhnValid()
  }

  fun getCVVLength() : Int {
    return meta.cvvLength
  }

  private fun isLuhnValid() : Boolean {
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
    fun fromString(str: String): CardNumber {
      return CardNumber(str.replace(INVALID_CHARACTER, ""))
    }
  }
}