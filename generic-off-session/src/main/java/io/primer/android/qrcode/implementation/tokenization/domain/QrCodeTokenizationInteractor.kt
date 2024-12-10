package io.primer.android.qrcode.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams

internal typealias QrCodeTokenizationInteractor = TokenizationInteractor<QrCodePaymentInstrumentParams>

internal class DefaultQrCodeTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<QrCodePaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter
) : TokenizationInteractor<QrCodePaymentInstrumentParams>(
    tokenizationRepository = tokenizationRepository,
    tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
    preTokenizationHandler = preTokenizationHandler,
    logReporter = logReporter
)
