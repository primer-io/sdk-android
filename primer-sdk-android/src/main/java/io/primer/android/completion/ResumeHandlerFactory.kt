package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ResumeHandlerFactory(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentResultRepository: PaymentResultRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val config: PrimerConfig,
    private val paymentMethodDataHelper: PrimerPaymentMethodDataHelper
) {

    fun getResumeHandler(paymentInstrumentType: String): PrimerResumeDecisionHandler {
        return when (paymentInstrumentType) {
            CARD_INSTRUMENT_TYPE -> ThreeDsPrimerResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                paymentResultRepository,
                analyticsRepository,
                threeDsSdkClassValidator,
                errorEventResolver,
                eventDispatcher,
                logger,
                config,
                paymentMethodDataHelper
            )
            ASYNC_PAYMENT_METHOD -> AsyncPaymentPrimerResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                paymentResultRepository,
                analyticsRepository,
                errorEventResolver,
                eventDispatcher,
                logger,
                config,
                paymentMethodDataHelper
            )
            else -> DefaultPrimerResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                paymentResultRepository,
                analyticsRepository,
                errorEventResolver,
                eventDispatcher,
                logger,
                config,
                paymentMethodDataHelper
            )
        }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
        const val ASYNC_PAYMENT_METHOD = "OFF_SESSION_PAYMENT"
    }
}
