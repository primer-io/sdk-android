package io.primer.android.core.data.model

data class BaseRemoteHostRequest<T>(val host: String, val data: T)
