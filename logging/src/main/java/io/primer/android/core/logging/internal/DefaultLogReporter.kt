package io.primer.android.core.logging.internal

import io.primer.android.core.logging.PrimerLog
import io.primer.android.core.logging.PrimerLogLevel
import io.primer.android.core.logging.PrimerLogging

/**
 * Defines methods for reporting logs at different levels.
 */
interface LogReporter {
    /**
     * Specifies the current log level for this reporter.
     */
    val logLevel: PrimerLogLevel

    /**
     * Logs a message at the DEBUG level.
     * @param message The message to be logged.
     */
    fun debug(
        message: String,
        component: String? = null,
    )

    /**
     * Logs a message at the INFO level.
     * @param message The message to be logged.
     */
    fun info(
        message: String,
        component: String? = null,
    )

    /**
     * Logs a message at the WARN level.
     * @param message The message to be logged.
     */
    fun warn(
        message: String,
        component: String? = null,
    )

    /**
     * Logs an error message at the ERROR level with an optional throwable.
     * @param message The error message to be logged.
     * @param throwable An optional Throwable associated with the error.
     */
    fun error(
        message: String,
        component: String? = null,
        throwable: Throwable? = null,
    )
}

class DefaultLogReporter : LogReporter {
    override val logLevel: PrimerLogLevel
        get() = PrimerLogging.logger.logLevel

    override fun debug(
        message: String,
        component: String?,
    ) = log(
        PrimerLog.Debug(
            PrimerLogLevel.DEBUG,
            listOfNotNull(component, message).joinToString(LOG_SEPARATOR),
        ),
    )

    override fun info(
        message: String,
        component: String?,
    ) = log(
        PrimerLog.Info(
            PrimerLogLevel.INFO,
            listOfNotNull(component, message).joinToString(LOG_SEPARATOR),
        ),
    )

    override fun warn(
        message: String,
        component: String?,
    ) = log(
        PrimerLog.Warning(
            PrimerLogLevel.WARNING,
            listOfNotNull(component, message).joinToString(LOG_SEPARATOR),
        ),
    )

    override fun error(
        message: String,
        component: String?,
        throwable: Throwable?,
    ) = log(
        PrimerLog.Error(
            PrimerLogLevel.ERROR,
            listOfNotNull(component, message).joinToString(LOG_SEPARATOR),
            throwable,
        ),
    )

    private fun log(primerLog: PrimerLog) {
        if (primerLog.logLevel >= this.logLevel) {
            PrimerLogging.logger.log(primerLog)
        }
    }

    private companion object {
        const val LOG_SEPARATOR = ": "
    }
}
