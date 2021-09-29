package io.primer.android.logging

import android.util.Log

internal interface Logger {

    fun info(message: String)

    fun warn(message: String)

    fun error(message: String, tr: Throwable? = null)
}

internal class DefaultLogger(private val tag: String) : Logger {

    operator fun invoke(message: String) {
        info(message)
    }

    override fun info(message: String) {
        Log.i(tag, message)
    }

    override fun warn(message: String) {
        Log.w(tag, message)
    }

    override fun error(message: String, tr: Throwable?) {
        Log.e("primer.$tag", message, tr)
    }
}
