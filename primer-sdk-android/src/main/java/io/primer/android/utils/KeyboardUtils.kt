package io.primer.android.utils

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

fun View?.hideKeyboard() {
    this ?: return
    val inputMethodManager = ContextCompat.getSystemService(context, InputMethodManager::class.java)
    inputMethodManager?.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
