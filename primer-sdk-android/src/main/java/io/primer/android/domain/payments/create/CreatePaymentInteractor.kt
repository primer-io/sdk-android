package io.primer.android.domain.payments.create

import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.create.repository.CreatePaymentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class CreatePaymentInteractor(
    private val createPaymentsRepository: CreatePaymentRepository,
    private val paymentResultEventsResolver: PaymentResultEventsResolver,
    private val errorEventResolver: BaseErrorEventResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<String, CreatePaymentParams>() {

    override fun execute(params: CreatePaymentParams): Flow<String> {
        return createPaymentsRepository.createPayment(params.token)
            .onEach {
                paymentResultEventsResolver.resolve(it, params.resumeHandler)
            }
            .catch {
                errorEventResolver.resolve(it, ErrorMapperType.PAYMENT_CREATE)
            }
            .flowOn(dispatcher)
            .map { it.payment.id }
    }
}
