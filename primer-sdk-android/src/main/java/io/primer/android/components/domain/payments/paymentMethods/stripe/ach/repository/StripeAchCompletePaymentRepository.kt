package io.primer.android.components.domain.payments.paymentMethods.stripe.ach.repository

internal fun interface StripeAchCompletePaymentRepository {

    suspend fun completePayment(
        completeUrl: String,
        mandateTimestamp: String,
        paymentMethodId: String?
    ): Result<Unit>
}
