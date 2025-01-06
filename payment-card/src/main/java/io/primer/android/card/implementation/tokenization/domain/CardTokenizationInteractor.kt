package io.primer.android.card.implementation.tokenization.domain

import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias CardTokenizationInteractor = TokenizationInteractor<CardPaymentInstrumentParams>

internal class DefaultCardTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<CardPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<CardPaymentInstrumentParams>(
        tokenizationRepository = tokenizationRepository,
        tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
        preTokenizationHandler = preTokenizationHandler,
        logReporter = logReporter,
    )
