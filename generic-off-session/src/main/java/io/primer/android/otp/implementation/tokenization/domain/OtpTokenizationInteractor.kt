package io.primer.android.otp.implementation.tokenization.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal typealias OtpTokenizationInteractor = TokenizationInteractor<OtpPaymentInstrumentParams>

internal class DefaultOtpTokenizationInteractor(
    tokenizationRepository: TokenizationRepository<OtpPaymentInstrumentParams>,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    preTokenizationHandler: PreTokenizationHandler,
    logReporter: LogReporter,
) : TokenizationInteractor<OtpPaymentInstrumentParams>(
        tokenizationRepository = tokenizationRepository,
        tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
        preTokenizationHandler = preTokenizationHandler,
        logReporter = logReporter,
    )
