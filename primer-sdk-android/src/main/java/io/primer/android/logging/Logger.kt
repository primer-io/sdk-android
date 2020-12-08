package io.primer.android.logging

import android.util.Log

internal class Logger(private val tag: String) {
  operator fun invoke(message: String) {
    info(message)
  }

  fun info(message: String) {
    Log.i(tag, message)
  }

  fun warn(message: String) {
    Log.w(tag, message)
  }

  fun error(message: String, tr: Throwable? = null) {
    Log.e("primer.$tag", message, tr)
  }
}