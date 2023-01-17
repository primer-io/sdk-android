package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models

internal data class KlarnaSession(
    val webViewTitle: String,
    val sessionId: String,
    val clientToken: String,
)
