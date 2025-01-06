package io.primer.android.core.logging

/**
 * Enumeration representing different logging levels for the Primer SDK.
 *
 * The `PrimerLogLevel` enum defines the following logging levels:
 * - [NONE]: No logging is performed.
 * - [DEBUG]: Debugging information is logged, useful for development and debugging.
 * - [INFO]: Informational messages are logged, providing general runtime information.
 * - [WARNING]: Warning messages are logged, indicating potential issues that may need attention.
 * - [ERROR]: Error messages are logged, indicating critical issues or failures.
 */
enum class PrimerLogLevel {
    /**
     * Debugging information is logged, useful for development and debugging.
     */
    DEBUG,

    /**
     * Informational messages are logged, providing general runtime information.
     */
    INFO,

    /**
     * Warning messages are logged, indicating potential issues that may need attention.
     */
    WARNING,

    /**
     * Error messages are logged, indicating critical issues or failures.
     */
    ERROR,

    /**
     * No logging is performed.
     */
    NONE,
}
