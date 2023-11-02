package io.primer.android.domain.payments.resume

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class ResumePaymentInteractor(
    private val resumePaymentsRepository: ResumePaymentsRepository,
    private val paymentResultEventsResolver: PaymentResultEventsResolver,
    private val logReporter: LogReporter,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    BaseFlowInteractor<Unit, ResumeParams>() {
    override fun execute(params: ResumeParams): Flow<Unit> {
        return resumePaymentsRepository.resumePayment(params.id, params.token)
            .onStart {
                logReporter.debug("Resuming payment with id: ${params.id}")
            }
            .onEach {
                paymentResultEventsResolver.resolve(it, params.resumeHandler)
            }
            .catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.PAYMENT_RESUME)
            }
            .flowOn(dispatcher)
            .map { }
    }
}
