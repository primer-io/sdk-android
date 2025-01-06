package io.primer.android.core.logging

/**
 * Singleton object used for managing the logging functionality within the SDK and application.
 */
object PrimerLogging {
    /**
     * The logger property responsible for handling logging operations.
     * By default, it's initialized with a [ConsolePrimerLogger].
     */
    var logger: PrimerLogger = ConsolePrimerLogger()
}
