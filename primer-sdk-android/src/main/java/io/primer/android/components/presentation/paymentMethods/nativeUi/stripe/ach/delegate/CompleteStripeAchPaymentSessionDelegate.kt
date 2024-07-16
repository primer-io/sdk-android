package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.StripeAchCompletePaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentParams
import io.primer.android.core.extensions.toIso8601String
import java.util.Date

internal class CompleteStripeAchPaymentSessionDelegate(
    private val stripeAchCompletePaymentInteractor: StripeAchCompletePaymentInteractor
) {
    suspend operator fun invoke(
        completeUrl: String,
        paymentMethodId: String?,
        mandateTimestamp: Date
    ): Result<Unit> = stripeAchCompletePaymentInteractor.invoke(
        StripeAchCompletePaymentParams(
            completeUrl = completeUrl,
            mandateTimestamp = mandateTimestamp.toIso8601String(),
            paymentMethodId = paymentMethodId
        )
    )
}
