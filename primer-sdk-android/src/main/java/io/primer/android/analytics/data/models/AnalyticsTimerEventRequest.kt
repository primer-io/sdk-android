package io.primer.android.analytics.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsTimerEventRequest(
    override val deviceData: DeviceData,
    override val properties: TimerProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.TIMER_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsTimerEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class TimerProperties(val id: TimerId, val timerType: TimerType) :
    BaseAnalyticsProperties()

internal enum class TimerType {
    START,
    END
}

internal enum class TimerId {
    CHECKOUT_DURATION,
}
