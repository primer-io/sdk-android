package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ResumeHandlerFactory(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger
) {

    fun getResumeHandler(paymentInstrumentType: String): ResumeDecisionHandler {
        return when (paymentInstrumentType) {
            CARD_INSTRUMENT_TYPE -> ThreeDsResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
                threeDsSdkClassValidator,
                errorEventResolver,
                eventDispatcher,
                logger
            )
            ASYNC_PAYMENT_METHOD -> AsyncPaymentResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
                errorEventResolver,
                eventDispatcher,
                logger
            )
            else -> DefaultResumeDecisionHandler(
                validationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
                errorEventResolver,
                eventDispatcher,
                logger
            )
        }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
        const val ASYNC_PAYMENT_METHOD = "OFF_SESSION_PAYMENT"
    }
}
