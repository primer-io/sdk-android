package io.primer.android.components.domain.payments.paymentMethods.stripe.ach

import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentParams
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.repository.StripeAchCompletePaymentRepository
import io.primer.android.domain.base.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class StripeAchCompletePaymentInteractor(
    private val completePaymentRepository: StripeAchCompletePaymentRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, StripeAchCompletePaymentParams>() {
    override suspend fun performAction(params: StripeAchCompletePaymentParams) =
        completePaymentRepository.completePayment(
            completeUrl = params.completeUrl,
            mandateTimestamp = params.mandateTimestamp,
            paymentMethodId = params.paymentMethodId
        )
}
