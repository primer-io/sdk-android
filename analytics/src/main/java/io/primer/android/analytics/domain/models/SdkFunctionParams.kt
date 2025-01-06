package io.primer.android.analytics.domain.models

data class SdkFunctionParams(
    val name: String,
    val params: Map<String, Any> = hashMapOf(),
) : BaseAnalyticsParams()
