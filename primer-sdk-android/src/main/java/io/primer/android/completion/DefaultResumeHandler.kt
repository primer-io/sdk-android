package io.primer.android.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.token.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toResumeErrorEvent
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal open class DefaultResumeHandler(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val eventDispatcher: EventDispatcher,
    private var logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ResumeHandler {

    private var handlerUsed = false

    override fun handleError(error: Error) = callIfNotHandled {
        addAnalyticsEvent("handleError")
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED)
        )
    }

    override fun handleSuccess() = callIfNotHandled {
        addAnalyticsEvent("handleSuccess")
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowSuccess(successType = SuccessType.PAYMENT_SUCCESS)
        )
    }

    override fun handleNewClientToken(clientToken: String) = callIfNotHandled {
        addAnalyticsEvent("handleNewClientToken")
        try {
            CoroutineScope(dispatcher).launch {
                validationTokenRepository.validate(clientToken).catch { e ->
                    eventDispatcher.dispatchEvent(e.toResumeErrorEvent())
                }.collect {
                    clientTokenRepository.setClientToken(clientToken)
                    handleClientToken(clientToken)
                }
            }
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

    protected open fun handleClientToken(clientToken: String) {
        checkCorrectFlowLaunched()
    }

    private fun checkCorrectFlowLaunched() {
        val clientTokenIntent = clientTokenRepository.getClientTokenIntent()
        require(
            getPaymentInstrumentType().intents?.contains(clientTokenIntent) == true ||
                getPaymentMethodType().intents?.contains(clientTokenIntent) == true
        ) { RESUME_INTENT_ERROR }
    }

    private fun getPaymentInstrumentType() = PaymentMethodType.safeValueOf(
        paymentMethodRepository.getPaymentMethod().paymentInstrumentType
    )

    private fun getPaymentMethodType() = PaymentMethodType.safeValueOf(
        paymentMethodRepository.getPaymentMethod()
            .paymentInstrumentData?.paymentMethodType
    )

    private fun callIfNotHandled(function: () -> Unit) = synchronized(this) {
        if (handlerUsed.not()) {
            handlerUsed = true
            function()
        } else {
            logger.warn(HANDLER_USED_ERROR)
        }
    }

    private fun addAnalyticsEvent(name: String) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                name,
                mapOf("class" to javaClass.simpleName)
            )
        )
    }

    protected companion object {

        const val HANDLER_USED_ERROR = "ResumeHandler can be used only once."
        const val RESUME_INTENT_ERROR = "Unexpected client token intent"
    }
}
