package io.primer.android.data.base.models

import io.primer.android.data.configuration.model.Configuration

internal data class BaseRemoteRequest<T>(val configuration: Configuration, val data: T)

internal data class BaseRemoteUrlRequest<T>(val url: String, val data: T)
