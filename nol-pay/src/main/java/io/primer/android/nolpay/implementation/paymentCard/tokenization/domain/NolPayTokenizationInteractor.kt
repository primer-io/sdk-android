package io.primer.android.nolpay.implementation.paymentCard.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias NolPayTokenizationInteractor = TokenizationInteractor<NolPayPaymentInstrumentParams>

internal class DefaultNolPayTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<NolPayPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter
) : TokenizationInteractor<NolPayPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter
)
