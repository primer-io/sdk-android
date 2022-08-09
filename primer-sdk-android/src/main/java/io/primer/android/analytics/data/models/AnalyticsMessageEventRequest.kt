package io.primer.android.analytics.data.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsMessageEventRequest(
    override val device: DeviceData,
    override val properties: MessageProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    @EncodeDefault
    override val eventType: AnalyticsEventType = AnalyticsEventType.MESSAGE_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsMessageEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class MessageProperties(
    val messageType: MessageType,
    val message: String,
    val severity: Severity,
    val diagnosticsId: String? = null
) : BaseAnalyticsProperties()

internal enum class MessageType {
    VALIDATION_FAILED, ERROR, PM_IMAGE_LOADING_FAILED
}

internal enum class Severity {
    INFO, WARN, ERROR
}
