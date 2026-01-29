package io.primer.android.components.analytics.data.model

internal data class SessionMetadata(
    val integrationType: IntegrationType?,
    val availablePaymentMethods: List<String>?,
)
