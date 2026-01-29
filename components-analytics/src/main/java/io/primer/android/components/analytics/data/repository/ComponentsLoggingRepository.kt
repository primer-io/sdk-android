package io.primer.android.components.analytics.data.repository

/**
 * Repository for sending log events to Datadog /v1/sdk-logs endpoint.
 *
 * CRITICAL: Kotlin code uses camelCase (convention), but JSON payloads use snake_case
 * field naming to match iOS implementation.
 */
interface ComponentsLoggingRepository {

    /**
     * Send an INFO-level log event.
     *
     * USE ONLY ONCE per session for SDK initialization timing.
     *
     * @param message Log message
     * @param event Event name (e.g., "checkout_components_initialized") - REQUIRED
     * @param initDurationMs SDK initialization duration in milliseconds
     */
    fun sendInfoLog(
        message: String,
        event: String,
        initDurationMs: Long? = null,
    )

    /**
     * Start observing crash events from the uncaught exception handler.
     *
     * This should be called once after the repository is created to begin
     * monitoring for SDK crashes and automatically send them to Datadog.
     */
    fun observeCrashes()
}
