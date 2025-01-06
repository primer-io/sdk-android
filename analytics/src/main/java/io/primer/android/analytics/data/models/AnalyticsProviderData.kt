package io.primer.android.analytics.data.models

internal data class AnalyticsProviderData(
    val applicationId: String,
    val data: AnalyticsData?,
)

data class AnalyticsData(
    val sdkIntegrationType: SdkIntegrationType?,
    val paymentHandling: String?,
    val analyticsUrl: String?,
    val clientSessionId: String?,
    val orderId: String?,
    val primerAccountId: String?,
)
