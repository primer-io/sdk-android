package io.primer.android.completion

import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository

internal class AsyncPaymentResumeHandler(
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val eventDispatcher: EventDispatcher,
    logger: Logger
) : DefaultResumeHandler(clientTokenRepository, paymentMethodRepository, eventDispatcher, logger) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        eventDispatcher.dispatchEvent(
            CheckoutEvent.StartAsyncFlow(
                clientTokenRepository.getRedirectUrl().orEmpty(),
                clientTokenRepository.getStatusUrl().orEmpty()
            )
        )
    }
}
