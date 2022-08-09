package io.primer.android.data.base.models

import io.primer.android.data.configuration.models.ConfigurationData

internal data class BaseRemoteRequest<T>(val configuration: ConfigurationData, val data: T)

internal data class BaseRemoteUrlRequest<T>(val url: String, val data: T)
