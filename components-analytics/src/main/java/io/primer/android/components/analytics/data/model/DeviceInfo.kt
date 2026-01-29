package io.primer.android.components.analytics.data.model

internal data class DeviceInfo(
    val model: String,
    val osVersion: String,
    val locale: String,
    val timezone: String,
    val networkType: String?,
)
