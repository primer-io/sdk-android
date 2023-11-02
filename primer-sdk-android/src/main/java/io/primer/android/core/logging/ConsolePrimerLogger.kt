package io.primer.android.core.logging

import android.util.Log
import io.primer.android.BuildConfig

internal class ConsolePrimerLogger : PrimerLogger {

    override var logLevel: PrimerLogLevel =
        if (BuildConfig.DEBUG) PrimerLogLevel.DEBUG else PrimerLogLevel.NONE

    override fun log(primerLog: PrimerLog) = when (primerLog) {
        is PrimerLog.Debug -> logDebug(primerLog.message)
        is PrimerLog.Info -> logInfo(primerLog.message)
        is PrimerLog.Warning -> logWarning(primerLog.message)
        is PrimerLog.Error -> logError(primerLog.message, primerLog.throwable)
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    private fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    private fun logWarning(message: String) {
        Log.w(TAG, message)
    }

    private fun logError(message: String, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }

    private companion object {

        const val TAG = "PrimerSDK"
    }
}
