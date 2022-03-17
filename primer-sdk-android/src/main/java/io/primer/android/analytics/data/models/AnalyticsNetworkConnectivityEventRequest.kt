package io.primer.android.analytics.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsNetworkConnectivityEventRequest(
    override val deviceData: DeviceData,
    override val properties: NetworkTypeProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.NETWORK_CONNECTIVITY_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsNetworkConnectivityEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class NetworkTypeProperties(
    val networkType: NetworkType,
) : BaseAnalyticsProperties()

@Serializable
internal enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}
