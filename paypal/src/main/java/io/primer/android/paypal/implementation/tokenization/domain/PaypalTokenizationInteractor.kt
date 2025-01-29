package io.primer.android.paypal.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams

internal typealias PaypalTokenizationInteractor = TokenizationInteractor<PaypalPaymentInstrumentParams>

internal class DefaultPaypalTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<PaypalPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<PaypalPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter,
)
