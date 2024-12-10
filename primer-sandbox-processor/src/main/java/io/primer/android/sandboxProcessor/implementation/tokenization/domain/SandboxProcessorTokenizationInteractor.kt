package io.primer.android.sandboxProcessor.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.model.SandboxProcessorPaymentInstrumentParams

internal typealias ProcessorTestTokenizationInteractor = TokenizationInteractor<SandboxProcessorPaymentInstrumentParams>

internal class DefaultProcessorTestTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<SandboxProcessorPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter
) : TokenizationInteractor<SandboxProcessorPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter
)
