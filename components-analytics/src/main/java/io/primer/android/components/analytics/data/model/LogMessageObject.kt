package io.primer.android.components.analytics.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Log message object that gets JSON-stringified and placed in LogEvent.message field.
 *
 * CRITICAL: Kotlin field names use camelCase (Kotlin convention), but toJsonString() outputs
 * snake_case JSON to match iOS implementation and spec.
 */
internal data class LogMessageObject(
    // Root fields
    val message: String,
    val status: String, // "info" | "warn" | "error"
    val event: String? = null, // Event name (INFO logs only), e.g., "checkout_components_initialized"

    // INFO log field (ONLY for SDK init)
    val initDurationMs: Long? = null,

    // Error-specific fields
    val errorMessage: String? = null,
    val errorStack: String? = null,

    // Nested objects
    val primer: PrimerInfo,
    val deviceInfo: DeviceInfo,
    val appMetadata: AppMetadata? = null,
    val sessionMetadata: SessionMetadata? = null,
) {
    @Suppress("CyclomaticComplexMethod")
    fun toJsonString(): String {
        return JSONObject().apply {
            put("message", message)
            put("status", status)

            event?.let { put("event", it) }
            initDurationMs?.let { put("init_duration_ms", it) }
            errorMessage?.let { put("error_message", it) }
            errorStack?.let { put("error_stack", it) }

            // Nested primer object (snake_case in JSON)
            put(
                "primer",
                JSONObject().apply {
                    put("checkout_session_id", primer.checkoutSessionId)
                    primer.clientSessionId?.let { put("client_session_id", it) }
                    primer.primerAccountId?.let { put("primer_account_id", it) }
                    primer.customerId?.let { put("customer_id", it) }
                },
            )

            // Nested device_info object (snake_case in JSON)
            put(
                "device_info",
                JSONObject().apply {
                    deviceInfo.model?.let { put("model", it) }
                    deviceInfo.osVersion?.let { put("os_version", it) }
                    put("locale", deviceInfo.locale)
                    put("timezone", deviceInfo.timezone)
                    deviceInfo.networkType?.let { put("network_type", it) }
                },
            )

            // Optional app_metadata (snake_case in JSON)
            appMetadata?.let { metadata ->
                put(
                    "app_metadata",
                    JSONObject().apply {
                        put("app_name", metadata.appName)
                        put("app_version", metadata.appVersion)
                        put("app_id", metadata.appId)
                    },
                )
            }

            // Optional session_metadata (snake_case in JSON)
            sessionMetadata?.let { session ->
                put(
                    "session_metadata",
                    JSONObject().apply {
                        session.integrationType?.let { put("integration_type", it.value) }
                        session.availablePaymentMethods?.let { methods ->
                            put("available_payment_methods", JSONArray(methods))
                        }
                    },
                )
            }
        }.toString()
    }
}
