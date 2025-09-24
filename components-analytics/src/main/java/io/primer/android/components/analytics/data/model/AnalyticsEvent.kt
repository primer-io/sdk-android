package io.primer.android.components.analytics.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.utils.SdkTypeResolver
import org.json.JSONObject

/**
 * Data model for analytics events sent to the SDK analytics endpoint.
 * Based on the JSON schema for payment flow events.
 */
data class AnalyticsEvent(
    // Required fields
    val id: String, // Unique ID of the event, use V7 UUID generation
    val eventName: String, // The name of the event
    val timeInSeconds: Long, // UNIX / Epoch timestamp as integer
    val checkoutSessionId: String, // Session ID that is assigned at the first event
    val clientSessionId: String, // UUID
    val sdkType: String = SdkTypeResolver.resolve().name, // Automatically detected: ANDROID_NATIVE or RN_ANDROID
    val sdkVersion: String, // Current SDK version, semver without any prefix
    val primerAccountId: String, // Primer identifier for the merchant (UUID)

    // Optional fields
    val userAgent: String? = null, // Web user agent
    val browser: String? = null, // Main browser name from user agent
    val device: String? = null, // Device make and model
    val deviceType: String? = null, // Type of device (laptop, phone, tablet, etc.)
    val eventType: String? = null, // Type of event for future proofing
    val userLocale: String? = null, // Locale in ISO format (e.g., en-GB)
    val paymentMethod: String? = null, // The selected payment method
    val paymentId: String? = null, // Received at success/failure
    val redirectDestinationUrl: String? = null, // Redirect 3rd party URL
    val threedsProvider: String? = null, // 3DS provider
    val threedsResponse: String? = null, // ECI value or other 3DS info
) : JSONObjectSerializable {

    companion object {
        @JvmField
        val serializer = JSONObjectSerializer<AnalyticsEvent> { event ->
            JSONObject().apply {
                // Required fields
                put("id", event.id)
                put("eventName", event.eventName)
                put("timestamp", event.timeInSeconds)
                put("checkoutSessionId", event.checkoutSessionId)
                put("clientSessionId", event.clientSessionId)
                put("sdkType", event.sdkType)
                put("sdkVersion", event.sdkVersion)
                put("primerAccountId", event.primerAccountId)

                // Optional fields - only add if not null
                event.userAgent?.let { put("userAgent", it) }
                event.browser?.let { put("browser", it) }
                event.device?.let { put("device", it) }
                event.deviceType?.let { put("deviceType", it) }
                event.eventType?.let { put("eventType", it) }
                event.userLocale?.let { put("userLocale", it) }
                event.paymentMethod?.let { put("paymentMethod", it) }
                event.paymentId?.let { put("paymentId", it) }
                event.redirectDestinationUrl?.let { put("redirectDestinationUrl", it) }
                event.threedsProvider?.let { put("threedsProvider", it) }
                event.threedsResponse?.let { put("threedsResponse", it) }
            }
        }
    }
}
