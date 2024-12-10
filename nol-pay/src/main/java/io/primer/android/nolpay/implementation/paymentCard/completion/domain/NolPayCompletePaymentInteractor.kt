package io.primer.android.nolpay.implementation.paymentCard.completion.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayCompletePaymentParams
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayCompletePaymentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayCompletePaymentInteractor(
    private val completePaymentRepository: NolPayCompletePaymentRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, NolPayCompletePaymentParams>() {
    override suspend fun performAction(params: NolPayCompletePaymentParams) =
        completePaymentRepository.completePayment(params.completeUrl)
}
