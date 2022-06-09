package io.primer.android.analytics.data.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsSdkFunctionEventRequest(
    override val device: DeviceData? = null,
    override val properties: FunctionProperties,
    override val appIdentifier: String? = null,
    override val sdkSessionId: String,
    override val checkoutSessionId: String? = null,
    override val clientSessionId: String? = null,
    override val orderId: String? = null,
    override val primerAccountId: String? = null,
    override val analyticsUrl: String? = null,
    @EncodeDefault
    override val eventType: AnalyticsEventType = AnalyticsEventType.SDK_FUNCTION_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsSdkFunctionEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class FunctionProperties(
    val name: String,
    val params: Map<String, String>,
) : BaseAnalyticsProperties()
