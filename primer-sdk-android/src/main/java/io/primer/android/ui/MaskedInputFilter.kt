package io.primer.android.ui

import android.text.InputFilter
import android.text.Spanned
import io.primer.android.logging.Logger

internal class MaskedInputFilter: InputFilter {
  val log = Logger("input-mask")

  override fun filter(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    log("source: $source, start: $start, end: $end, dest: $dest, dstart: $dstart, dend: $dend")

    // Ignore all non digit input
    if (!source.matches(Regex("^\\d+$"))) {
      return null
    }

    val len = end - start
    val isPaste = len > 1
    val isDelete = len == 0
    val isType = len == 1

    // When there's no input yet and the user hits the first char
    if (isType && dest.length == 0) {
      return handleFirstChar(source.toString())
    }

    return source
  }

  private fun handleFirstChar(char: String): String {

    // If the user input
    if (char.matches(Regex("[2-9]"))) {
      return "0$char / "
    }

    return char
  }

//  private fun format() {
//
//  }

//  private fun handleInput(input: CharSequence, before: CharSequence, after: )

//  private fun handleDelete(dest: Spanned, from: Int, to: Int): CharSequence {
//    TODO("NOT IMPLMENMTED")
//  }

//  private fun handlePaste(source: CharSequence, dest: Spanned, )
}