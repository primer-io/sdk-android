package io.primer.android.domain.payments.apaya.models

internal data class ApayaSession(
    val webViewTitle: String?,
    val redirectUrl: String,
    val token: String,
)
