package io.primer.components.domain.models

/**
 * Represents the possible statuses of a payment.
 */
enum class PaymentStatus {
    /** The payment was successfully completed. */
    COMPLETED,

    /** The payment failed. */
    FAILED,

    /** The payment was canceled by the user or system. */
    CANCELLED,
}
