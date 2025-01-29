package io.primer.android.banks.implementation.tokenization.domain

import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias BankIssuerTokenizationInteractor = TokenizationInteractor<BankIssuerPaymentInstrumentParams>

internal class DefaultBankIssuerTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<BankIssuerPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<BankIssuerPaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter,
)
