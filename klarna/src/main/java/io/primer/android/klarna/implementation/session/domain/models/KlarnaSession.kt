package io.primer.android.klarna.implementation.session.domain.models

data class KlarnaSession(
    val sessionId: String,
    val clientToken: String,
    val availableCategories: List<KlarnaPaymentCategory>
)
