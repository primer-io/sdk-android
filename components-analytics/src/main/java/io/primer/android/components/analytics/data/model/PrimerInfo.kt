package io.primer.android.components.analytics.data.model

internal data class PrimerInfo(
    val checkoutSessionId: String,
    val clientSessionId: String?,
    val primerAccountId: String?,
    val customerId: String? = null,
)
