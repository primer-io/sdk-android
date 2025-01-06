package io.primer.android.stripe.ach.implementation.payment.confirmation.domain.repository

internal fun interface StripeAchCompletePaymentRepository {
    suspend fun completePayment(
        completeUrl: String,
        mandateTimestamp: String,
        paymentMethodId: String?,
    ): Result<Unit>
}
