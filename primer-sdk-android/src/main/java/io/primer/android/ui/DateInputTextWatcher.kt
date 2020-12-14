package io.primer.android.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.primer.android.logging.Logger
import java.lang.StringBuilder


private val INVALID_CHARACTER = Regex("[^0-9/]")
private val ZERO_OR_ONE = Regex("^[01]$")
private val TWO_THROUGH_NINE = Regex("^[2-9]$")
private val VALID_MONTH = Regex("^(?:0[1-9]|1[0-2])$")
private val INVALID_MONTH = Regex("^[0-9]{3,}")
private val INVALID_YEAR = Regex("^.+(?:[/][0-9]{3,})$")


class DateInputTextWatcher private constructor (private val tv: TextView): TextWatcher{
  private val log = Logger("date-input-tw")
  private var editing = false
  private var isDelete = false

  override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

  override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    isDelete = before > 0
  }

  override fun afterTextChanged(s: Editable) {
    val input = s.toString()

    if (!editing) {
      val next = mask(input)
      val changed = input != next

      if (changed) {
        editing = true
        s.replace(0, input.length, next)
      } else {
        editing = false
      }
    } else {
      editing = false
    }
  }

  private fun mask(text: String): CharSequence {
    val sanitized = text.replace(INVALID_CHARACTER, "")

    if (sanitized.matches(ZERO_OR_ONE)) {
      return sanitized
    }

    if (sanitized.matches(TWO_THROUGH_NINE)) {
      return "0$sanitized/"
    }

    if (isDelete && sanitized.matches(VALID_MONTH)) {
      return sanitized
    }

    if (!isDelete && sanitized.matches(VALID_MONTH)) {
      return "$sanitized/"
    }

    if (sanitized.matches(INVALID_MONTH)) {
      return fromTokens(sanitized.split("/"))
    }

    if (sanitized.matches(INVALID_YEAR)) {
      return fromTokens(sanitized.split("/"))
    }

    return sanitized
  }

  private fun fromTokens(tokens: List<String>): String {
    val builder = StringBuilder()

    if (tokens.isNotEmpty()) {
      builder.append(tokens[0].substring(0, 2)).append("/")
    }

    if (tokens.size > 1) {
      builder.append(tokens[1].substring(0, 2))
    }

    return builder.toString()
  }

  companion object {
    fun attach(tv: TextView) {
      tv.addTextChangedListener(DateInputTextWatcher(tv))
    }
  }
}