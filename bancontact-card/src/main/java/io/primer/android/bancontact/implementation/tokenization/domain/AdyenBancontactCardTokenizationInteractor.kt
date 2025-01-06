package io.primer.android.bancontact.implementation.tokenization.domain

import io.primer.android.bancontact.implementation.tokenization.domain.model.AdyenBancontactPaymentInstrumentParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias AdyenBancontactTokenizationInteractor =
    TokenizationInteractor<AdyenBancontactPaymentInstrumentParams>

internal class DefaultAdyenBancontactTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<AdyenBancontactPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<AdyenBancontactPaymentInstrumentParams>(
        tokenizationRepository = tokenizationRepository,
        tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
        preTokenizationHandler = preTokenizationHandler,
        logReporter = logReporter,
    )
