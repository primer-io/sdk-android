package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.token.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ThreeDsResumeHandler(
    validationTokenRepository: ValidateTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    paymentMethodRepository: PaymentMethodRepository,
    analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val eventDispatcher: EventDispatcher,
    logger: Logger
) : DefaultResumeHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    analyticsRepository,
    eventDispatcher,
    logger
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        if (threeDsSdkClassValidator.is3dsSdkIncluded()) {
            eventDispatcher.dispatchEvent(
                CheckoutEvent.Start3DS
            )
        } else eventDispatcher.dispatchEvent(
            CheckoutEvent.ResumeError(
                APIError(ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR)
            )
        )
    }
}
