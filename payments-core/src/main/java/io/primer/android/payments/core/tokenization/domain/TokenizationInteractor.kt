package io.primer.android.payments.core.tokenization.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

abstract class TokenizationInteractor<T : BasePaymentInstrumentParams>(
    private val tokenizationRepository: TokenizationRepository<T>,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val preTokenizationHandler: PreTokenizationHandler,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PaymentMethodTokenInternal, TokenizationParams<T>>() {
    override suspend fun performAction(params: TokenizationParams<T>): Result<PaymentMethodTokenInternal> {
        val paymentMethodType = params.paymentInstrumentParams.paymentMethodType
        return preTokenizationHandler.handle(
            paymentMethodType = paymentMethodType,
            sessionIntent = params.sessionIntent,
        ).flatMap {
            logReporter.info(
                "Started tokenization for $paymentMethodType payment method.",
            )
            tokenizationRepository.tokenize(params).onSuccess { paymentMethodToken ->
                logReporter.info(
                    "Tokenization successful for $paymentMethodType payment method.",
                )
                tokenizedPaymentMethodRepository.setPaymentMethod(paymentMethodToken)
            }
        }
    }
}
