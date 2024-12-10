package io.primer.android.googlepay.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias GooglePayTokenizationInteractor = TokenizationInteractor<GooglePayPaymentInstrumentParams>

internal class DefaultGooglePayTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<GooglePayPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter
) : TokenizationInteractor<GooglePayPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter
)
