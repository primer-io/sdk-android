package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.payment.processor_3ds.Processor3DS
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ThreeDsResumeHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
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
            var processor3DSData: Processor3DS? = null
            if (clientTokenRepository.getClientTokenIntent() == ClientTokenIntent.PROCESSOR_3DS) {
                processor3DSData = Processor3DS(
                    clientTokenRepository.getRedirectUrl().orEmpty(),
                    clientTokenRepository.getStatusUrl().orEmpty()
                )
            }
            eventDispatcher.dispatchEvent(CheckoutEvent.Start3DS(processor3DSData))
        } else eventDispatcher.dispatchEvent(
            CheckoutEvent.ResumeError(
                APIError(ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR)
            )
        )
    }
}
