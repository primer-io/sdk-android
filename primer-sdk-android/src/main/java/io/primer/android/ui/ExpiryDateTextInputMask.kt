package io.primer.android.ui

class ExpiryDateTextInputMask: BaseTextInputMask() {
  override fun mask(value: String, deleting: Boolean): String {
    return ExpiryDate.fromString(value, autoInsert = !deleting).toString()
  }
}