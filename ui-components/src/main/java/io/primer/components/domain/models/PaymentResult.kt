package io.primer.components.domain.models

/**
 * Represents the result of a payment process.
 *
 * @param status The final [PaymentStatus] of the payment process.
 * @param transactionId The unique identifier for the transaction, if available.
 * @param error An optional error message in case of a failed payment.
 */
data class PaymentResult(
    val status: PaymentStatus,
    val transactionId: String? = null,
    val error: String? = null, // TODO TWS: move into PaymentStatus, turn PaymentStatus into a sealed interface.
)
