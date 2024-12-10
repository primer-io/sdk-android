package io.primer.android.paypal.implementation.tokenization.domain

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateOrderRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaypalCreateOrderInteractor(
    private val createOrderRepository: PaypalCreateOrderRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PaypalOrder, PaypalCreateOrderParams>() {

    override suspend fun performAction(params: PaypalCreateOrderParams): Result<PaypalOrder> {
        return createOrderRepository.createOrder(params)
    }
}
