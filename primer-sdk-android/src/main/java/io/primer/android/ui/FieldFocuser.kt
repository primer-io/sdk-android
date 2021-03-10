package io.primer.android.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class FieldFocuser {
    companion object {

        fun focus(view: View?) {
            view?.let {
                val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                it.requestFocus()

                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}
