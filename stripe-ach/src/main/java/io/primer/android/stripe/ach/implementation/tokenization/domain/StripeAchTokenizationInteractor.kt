package io.primer.android.stripe.ach.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams

internal class StripeAchTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<StripeAchPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<StripeAchPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter,
)
