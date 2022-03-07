package io.primer.android.analytics.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsDataRequest(val data: List<BaseAnalyticsEventRequest>)
