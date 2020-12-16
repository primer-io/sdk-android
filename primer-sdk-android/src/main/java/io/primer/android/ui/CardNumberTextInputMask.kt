package io.primer.android.ui

import android.text.TextWatcher

class CardNumberTextInputMask : BaseTextInputMask() {
  override fun mask(value: String, deleting: Boolean): String {
    return CardNumber.fromString(value).toString()
  }
}