package io.primer.android.payments.core.create.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.create.domain.model.CreatePaymentParams
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.create.domain.repository.CreatePaymentRepository
import io.primer.android.payments.core.helpers.PaymentDecisionResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

typealias CreatePaymentInteractor = BaseSuspendInteractor<PaymentDecision, CreatePaymentParams>

internal class DefaultCreatePaymentInteractor(
    private val createPaymentsRepository: CreatePaymentRepository,
    private val paymentDecisionResolver: PaymentDecisionResolver,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PaymentDecision, CreatePaymentParams>() {
    override suspend fun performAction(params: CreatePaymentParams): Result<PaymentDecision> {
        logReporter.debug("Creating payment for payment method token: ${params.token}")
        return createPaymentsRepository.createPayment(params.token)
            .map { paymentDecisionResolver.resolve(it) }
    }
}
