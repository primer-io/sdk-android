package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfo
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalInfoRepository
import io.primer.android.domain.base.BaseFlowInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalOrderInfoInteractor(
    private val paypalInfoRepository: PaypalInfoRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<PaypalOrderInfo, PaypalOrderInfoParams>() {

    override fun execute(params: PaypalOrderInfoParams): Flow<PaypalOrderInfo> {
        return paypalInfoRepository.getPaypalOrderInfo(params)
            .flowOn(dispatcher)
    }
}
