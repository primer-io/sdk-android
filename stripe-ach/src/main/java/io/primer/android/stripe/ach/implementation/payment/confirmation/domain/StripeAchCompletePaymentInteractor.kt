package io.primer.android.stripe.ach.implementation.payment.confirmation.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.model.StripeAchCompletePaymentParams
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.repository.StripeAchCompletePaymentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class StripeAchCompletePaymentInteractor(
    private val completePaymentRepository: StripeAchCompletePaymentRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, StripeAchCompletePaymentParams>() {
    override suspend fun performAction(params: StripeAchCompletePaymentParams) =
        completePaymentRepository.completePayment(
            completeUrl = params.completeUrl,
            mandateTimestamp = params.mandateTimestamp,
            paymentMethodId = params.paymentMethodId,
        )
}
