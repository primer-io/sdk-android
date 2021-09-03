package io.primer.android.domain.payments.apaya.models

internal data class ApayaSession(
    val redirectUrl: String,
    val token: String,
)
