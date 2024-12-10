package io.primer.android.payments.core.resume.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.PaymentDecisionResolver
import io.primer.android.payments.core.resume.domain.models.ResumeParams
import io.primer.android.payments.core.resume.domain.respository.ResumePaymentsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

typealias ResumePaymentInteractor = BaseSuspendInteractor<PaymentDecision, ResumeParams>

internal class DefaultResumePaymentInteractor(
    private val resumePaymentsRepository: ResumePaymentsRepository,
    private val paymentDecisionResolver: PaymentDecisionResolver,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PaymentDecision, ResumeParams>() {

    override suspend fun performAction(params: ResumeParams): Result<PaymentDecision> {
        logReporter.debug("Resuming payment with id: ${params.paymentId}")
        return resumePaymentsRepository.resumePayment(paymentId = params.paymentId, resumeToken = params.resumeToken)
            .map { paymentResult -> paymentDecisionResolver.resolve(paymentResult) }
    }
}
