package io.primer.android.ipay88.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias IPay88TokenizationInteractor = TokenizationInteractor<IPay88PaymentInstrumentParams>

internal class DefaultIPay88TokenizationInteractor(
    tokenizationRepository: TokenizationRepository<IPay88PaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<IPay88PaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter,
)
