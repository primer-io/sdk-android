package io.primer.android.core.logging

/**
 * Sealed interface [PrimerLog] defines various log types for the logging system.
 */
sealed interface PrimerLog {
    /** The log level associated with the log entry. */
    val logLevel: PrimerLogLevel

    /** The message to be logged. */
    val message: String

    /** Represents a debug log with its log level and message. */
    class Debug(override val logLevel: PrimerLogLevel, override val message: String) : PrimerLog

    /** Represents an informational log with its log level and message. */
    class Info(override val logLevel: PrimerLogLevel, override val message: String) : PrimerLog

    /** Represents a warning log with its log level and message. */
    class Warning(override val logLevel: PrimerLogLevel, override val message: String) : PrimerLog

    /**
     * Represents an error log with its log level, message, and an optional associated Throwable.
     *
     * @property throwable The associated Throwable (if any) that caused the error log.
     */
    class Error(
        override val logLevel: PrimerLogLevel,
        override val message: String,
        val throwable: Throwable?,
    ) : PrimerLog
}

/**
 * Interface [PrimerLogger] defines the contract for a logger implementation.
 */
interface PrimerLogger {
    /** The log level to control logging behavior. */
    var logLevel: PrimerLogLevel

    /**
     * Logs the provided [PrimerLog]`.
     *
     * @param primerLog The log entry to be processed and logged.
     */
    fun log(primerLog: PrimerLog)
}
