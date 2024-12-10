package io.primer.android.core.logging

import android.util.Log

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
        largeLog(tag = TAG, content = message, logAction = Log::d)
    }

    private fun logInfo(message: String) {
        largeLog(tag = TAG, content = message, logAction = Log::i)
    }

    private fun logWarning(message: String) {
        largeLog(tag = TAG, content = message, logAction = Log::w)
    }

    private fun logError(message: String, throwable: Throwable?) {
        largeLog(tag = TAG, content = message, throwable = throwable, logAction = Log::e)
    }

    private fun largeLog(
        tag: String,
        content: String,
        throwable: Throwable? = null,
        logAction: (String, String, Throwable?) -> Unit
    ) {
        var remainingContent = content
        while (remainingContent.length > MAX_LOG_SIZE_IN_CHARS) {
            logAction(tag, remainingContent.substring(0, MAX_LOG_SIZE_IN_CHARS), throwable)
            remainingContent = remainingContent.substring(MAX_LOG_SIZE_IN_CHARS)
        }
        // Log the final chunk or the entire content if it's smaller than MAX_LOG_SIZE_IN_CHARS
        if (remainingContent.isNotEmpty()) {
            logAction(tag, remainingContent, throwable)
        }
    }

    private companion object {

        const val TAG = "PrimerSDK"
        const val MAX_LOG_SIZE_IN_CHARS = 4096
    }
}
