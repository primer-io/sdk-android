package io.primer.android.analytics.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsCrashEventRequest(
    override val deviceData: DeviceData,
    override val properties: CrashProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.APP_CRASHED_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsCrashEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class CrashProperties(val stacktrace: List<String>) : BaseAnalyticsProperties()
