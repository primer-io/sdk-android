package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models

data class KlarnaSession(
    val webViewTitle: String,
    val sessionId: String,
    val clientToken: String,
    val availableCategories: List<KlarnaPaymentCategory>
)
