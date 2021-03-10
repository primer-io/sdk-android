package io.primer.android.ui

import android.text.Editable
import android.text.TextWatcher

internal abstract class TextInputMask : TextWatcher {

    private var deleting: Boolean = false
    private var editing: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        deleting = before > 0
    }

    override fun afterTextChanged(s: Editable) {
        if (editing) {
            editing = false
            return
        }

        val before = s.toString()
        val after = mask(before, deleting)

        if (before == after) {
            editing = false
            return
        }

        editing = true

        s.replace(0, before.length, after)
    }

    abstract fun mask(value: String, deleting: Boolean): String

    class CardNumber : TextInputMask() {

        override fun mask(value: String, deleting: Boolean): String {
            return CardNumberFormatter.fromString(value).toString()
        }
    }

    class ExpiryDate : TextInputMask() {

        override fun mask(value: String, deleting: Boolean): String {
            return ExpiryDateFormatter.fromString(value, autoInsert = !deleting).toString()
        }
    }
}
