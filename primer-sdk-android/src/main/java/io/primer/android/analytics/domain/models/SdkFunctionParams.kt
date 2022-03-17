package io.primer.android.analytics.domain.models

internal data class SdkFunctionParams(
    val name: String,
    val params: Map<String, String> = hashMapOf()
) : BaseAnalyticsParams()
