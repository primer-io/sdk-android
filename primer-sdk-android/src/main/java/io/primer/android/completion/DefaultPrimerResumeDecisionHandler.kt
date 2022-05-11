package io.primer.android.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
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

internal open class DefaultPrimerResumeDecisionHandler(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private var logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PrimerResumeDecisionHandler {

    private var handlerUsed = false

    override fun handleFailure(message: String?) = callIfNotHandled {
        addAnalyticsEvent("handleFailure")
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED, message = message)
        )
    }

    override fun handleSuccess() = callIfNotHandled {
        addAnalyticsEvent("handleSuccess")
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowSuccess(successType = SuccessType.PAYMENT_SUCCESS)
        )
    }

    override fun continueWithNewClientToken(clientToken: String) = callIfNotHandled {
        addAnalyticsEvent("handleNewClientToken")
        CoroutineScope(dispatcher).launch {
            validationTokenRepository.validate(clientToken).catch { e ->
                errorEventResolver.resolve(e, ErrorMapperType.PAYMENT_RESUME)
            }.collect {
                clientTokenRepository.setClientToken(clientToken)
                try {
                    handleClientToken(clientToken)
                } catch (e: IllegalArgumentException) {
                    errorEventResolver.resolve(e, ErrorMapperType.PAYMENT_RESUME)
                }
            }
        }
    }

    protected open fun handleClientToken(clientToken: String) {
        checkCorrectFlowLaunched()
    }

    private fun checkCorrectFlowLaunched() {
        require(
            getPaymentInstrumentType().intent == clientTokenRepository.getClientTokenIntent() ||
                getPaymentMethodType().intent == clientTokenRepository.getClientTokenIntent()
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
