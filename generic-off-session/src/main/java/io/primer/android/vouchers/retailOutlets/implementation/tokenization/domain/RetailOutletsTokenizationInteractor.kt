package io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams

internal typealias RetailOutletsTokenizationInteractor = TokenizationInteractor<RetailOutletsPaymentInstrumentParams>

internal class DefaultRetailOutletsTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<RetailOutletsPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<RetailOutletsPaymentInstrumentParams>(
        tokenizationRepository = tokenizationRepository,
        tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
        preTokenizationHandler = preTokenizationHandler,
        logReporter = logReporter,
    )
