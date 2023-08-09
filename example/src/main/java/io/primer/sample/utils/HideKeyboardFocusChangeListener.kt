package io.primer.sample.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

class HideKeyboardFocusChangeListener(
    val id: Int,
    val activity: FragmentActivity?,
) : View.OnFocusChangeListener {

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (v.id == id && !hasFocus) {
            v.let { activity?.hideKeyboard(it) }
        }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}