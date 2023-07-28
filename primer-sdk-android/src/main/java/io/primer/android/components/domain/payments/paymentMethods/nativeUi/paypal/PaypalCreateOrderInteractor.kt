package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrder
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateOrderRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalCreateOrderInteractor(
    private val createOrderRepository: PaypalCreateOrderRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<PaypalOrder, PaypalCreateOrderParams>() {
    override fun execute(params: PaypalCreateOrderParams): Flow<PaypalOrder> {
        return createOrderRepository.createOrder(params)
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
            .flowOn(dispatcher)
    }
}
