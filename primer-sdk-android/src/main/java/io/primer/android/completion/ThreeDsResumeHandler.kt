package io.primer.android.completion

import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError

internal class ThreeDsResumeHandler(
    private val clientTokenRepository: ClientTokenRepository,
    private val eventDispatcher: EventDispatcher,
    logger: Logger
) : DefaultResumeHandler(clientTokenRepository, eventDispatcher, logger) {

    override fun handleClientToken(clientToken: String) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION` -> eventDispatcher.dispatchEvent(
                CheckoutEvent.Start3DS
            )
            else -> eventDispatcher.dispatchEvent(
                CheckoutEvent.ResumeError(APIError(RESUME_INTENT_ERROR))
            )
        }
    }

    private companion object {
        const val RESUME_INTENT_ERROR = "Unexpected client token intent"
    }
}
