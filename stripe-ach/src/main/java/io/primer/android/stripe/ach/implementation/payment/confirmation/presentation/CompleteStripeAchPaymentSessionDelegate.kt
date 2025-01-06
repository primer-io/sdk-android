package io.primer.android.stripe.ach.implementation.payment.confirmation.presentation

import io.primer.android.core.extensions.toIso8601String
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.StripeAchCompletePaymentInteractor
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.model.StripeAchCompletePaymentParams
import java.util.Date

internal class CompleteStripeAchPaymentSessionDelegate(
    private val stripeAchCompletePaymentInteractor: StripeAchCompletePaymentInteractor,
) {
    suspend operator fun invoke(
        completeUrl: String,
        paymentMethodId: String?,
        mandateTimestamp: Date,
    ): Result<Unit> =
        stripeAchCompletePaymentInteractor.invoke(
            StripeAchCompletePaymentParams(
                completeUrl = completeUrl,
                mandateTimestamp = mandateTimestamp.toIso8601String(),
                paymentMethodId = paymentMethodId,
            ),
        )
}
