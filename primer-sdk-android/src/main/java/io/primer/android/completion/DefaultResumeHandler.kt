package io.primer.android.completion

import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import java.lang.Error

internal open class DefaultResumeHandler(
    private val clientTokenRepository: ClientTokenRepository,
    private val eventDispatcher: EventDispatcher,
    private var logger: Logger
) :
    ResumeHandler {

    private var handlerUsed = false

    override fun handleError(error: Error) = callIfNotHandled {
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED)
        )
    }

    override fun handleSuccess() = callIfNotHandled {
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowSuccess(successType = SuccessType.PAYMENT_SUCCESS)
        )
    }

    override fun handleNewClientToken(clientToken: String) = callIfNotHandled {
        try {
            clientTokenRepository.setClientToken(clientToken)
            handleClientToken(clientToken)
        } catch (e: IllegalArgumentException) {
            eventDispatcher.dispatchEvent(
                CheckoutEvent.ResumeError(
                    APIError.createDefaultWithMessage(
                        e.message.orEmpty()
                    )
                )
            )
        }
    }

    protected open fun handleClientToken(clientToken: String) = Unit

    private fun callIfNotHandled(function: () -> Unit) = synchronized(this) {
        if (handlerUsed.not()) {
            handlerUsed = true
            function()
        } else {
            logger.warn(HANDLER_USED_ERROR)
        }
    }

    private companion object {

        const val HANDLER_USED_ERROR = "ResumeHandler can be used only once."
    }
}
