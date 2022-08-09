package io.primer.android.domain.payments.paypal

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfo
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import io.primer.android.domain.payments.paypal.repository.PaypalInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalOrderInfoInteractor(
    private val paypalInfoRepository: PaypalInfoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<PaypalOrderInfo, PaypalOrderInfoParams>() {

    override fun execute(params: PaypalOrderInfoParams): Flow<PaypalOrderInfo> {
        return paypalInfoRepository.getPaypalOrderInfo(params)
            .flowOn(dispatcher)
    }
}
