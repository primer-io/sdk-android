package io.primer.sample.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import java.math.BigDecimal

class MoneyTextWatcher(editText: EditText) : TextWatcher {

    private val editTextWeakReference: WeakReference<EditText> = WeakReference(editText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val editText = editTextWeakReference.get() ?: return

        val s = editable.toString()

        if (s.isEmpty()) return

        editText.removeTextChangedListener(this)

        val cleanString = s.replace("[.\\s]".toRegex(), "").replace(",", ".")

        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)

        editText.setText(String.format(parsed.toString()))
        editText.setSelection(parsed.toString().length)
        editText.addTextChangedListener(this)
    }
}