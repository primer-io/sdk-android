package io.primer.android.ui

import android.text.InputFilter
import android.text.Spanned
import io.primer.android.logging.Logger

private val ZERO_OR_ONE = Regex("[01]")
private val ENDING_WITH_SEPARATOR = Regex("/$")
private val ONLY_DIGITS = Regex("^[0-9]+$")

class DateInputFilter : InputFilter {
  private val log = Logger("date-input-filter")

  override fun filter(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    val handler = when (end - start) {
      0 -> ::onDelete
      1 -> ::onType
      else -> ::onInsert
    }
    return handler(source, start, end, dest, dstart, dend)
  }

  private fun onDelete(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    val destString = dest.toString()

    log("Delete $destString")

    if (destString.matches(ENDING_WITH_SEPARATOR)) {
      log(destString.substring(0, destString.length - 1))
      return destString.substring(destString.length - 2)
    }

    return null
  }

  private fun onInsert(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    return null
  }

  private fun onType(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    val sourceString = source.toString()

    // Ignore anything that isn't a digit
    if (!isValidInput(sourceString)) {
      return ""
    }

    // If this is the first character we can make some assumptions about the month
    if (dest.isEmpty()) {
      // If the user typed a 0 or 1: we should wait for the next character
      if (sourceString.matches(ZERO_OR_ONE)) {
        return null
      }

      // Otherwise we can assume that they mean 03, 04, 05 ... etc
      return "0$sourceString/"
    }

    return null
  }

  private fun isValidInput(s: String): Boolean {
    return s.matches(ONLY_DIGITS)
  }
}