package io.primer.android.analytics.data.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsNetworkCallEvent(
    override val device: DeviceData,
    override val properties: NetworkCallProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    @EncodeDefault
    override val eventType: AnalyticsEventType = AnalyticsEventType.NETWORK_CALL_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsNetworkCallEvent = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class NetworkCallProperties(
    val networkCallType: NetworkCallType,
    val id: String,
    val url: String,
    val method: String,
    val responseCode: Int? = null,
    val errorBody: String? = null,
) : BaseAnalyticsProperties()

internal enum class NetworkCallType {
    REQUEST_START,
    REQUEST_END
}
