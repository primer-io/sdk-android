package io.primer.components.domain.models

data class PaymentResult(
    val status: PaymentStatus,
    val transactionId: String? = null,
    val error: String? = null,
)
