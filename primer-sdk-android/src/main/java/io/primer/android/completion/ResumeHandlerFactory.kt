package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ResumeHandlerFactory(
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger
) {

    fun getResumeHandler(paymentInstrumentType: String): ResumeHandler {
        return when (paymentInstrumentType) {
            CARD_INSTRUMENT_TYPE -> ThreeDsResumeHandler(
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
                threeDsSdkClassValidator,
                eventDispatcher,
                logger
            )
            ASYNC_PAYMENT_METHOD -> AsyncPaymentResumeHandler(
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
                eventDispatcher,
                logger
            )
            else -> DefaultResumeHandler(
                clientTokenRepository,
                paymentMethodRepository,
                analyticsRepository,
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
