package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCompletePaymentParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayCompletePaymentRepository
import io.primer.android.domain.base.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayCompletePaymentInteractor(
    private val completePaymentRepository: NolPayCompletePaymentRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, NolPayCompletePaymentParams>() {
    override suspend fun performAction(params: NolPayCompletePaymentParams) =
        completePaymentRepository.completePayment(params.completeUrl)
}
