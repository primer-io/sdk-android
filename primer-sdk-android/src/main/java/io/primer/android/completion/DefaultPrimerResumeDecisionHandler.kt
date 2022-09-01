package io.primer.android.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal open class DefaultPrimerResumeDecisionHandler(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentResultRepository: PaymentResultRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private var logger: Logger,
    private val config: PrimerConfig,
    private val paymentMethodDataHelper: PrimerPaymentMethodDataHelper,
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
                    handlePaymentMethodData(clientToken)
                } catch (e: IllegalArgumentException) {
                    errorEventResolver.resolve(e, ErrorMapperType.PAYMENT_RESUME)
                }
            }
        }
    }

    private fun handlePaymentMethodData(clientToken: String) {
        val clientTokenConfig = ClientToken.fromString(clientToken)
        if (clientTokenConfig.intent == ClientTokenIntent.PAYMENT_METHOD_VOUCHER.name) {
            if (
                config.settings.paymentHandling == PrimerPaymentHandling.MANUAL
            ) {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ResumePending(
                        paymentMethodDataHelper.prepareDataFromClientToken(clientTokenConfig)
                    )
                )
            } else {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.PaymentSuccess(
                        PrimerCheckoutData(
                            paymentResultRepository.getPaymentResult().payment,
                            paymentMethodDataHelper.prepareDataFromClientToken(clientTokenConfig)
                        )
                    )
                )
            }
        }
    }

    protected open fun handleClientToken(clientToken: String) {
        checkCorrectFlowLaunched()
    }

    private fun checkCorrectFlowLaunched() {
        val clientTokenIntent = clientTokenRepository.getClientTokenIntent()
        require(
            getPaymentInstrumentType().intents?.map { it.name }
                ?.contains(clientTokenIntent) == true ||
                "${getPaymentMethodTypeString()}$CLIENT_TOKEN_INTENT_SUFFIX" == clientTokenIntent ||
                getPaymentMethodType().intents?.map { it.name }?.contains(clientTokenIntent) == true
        ) { RESUME_INTENT_ERROR }
    }

    private fun getPaymentInstrumentType() = PaymentMethodType.safeValueOf(
        paymentMethodRepository.getPaymentMethod().paymentInstrumentType
    )

    private fun getPaymentMethodType() = PaymentMethodType.safeValueOf(
        paymentMethodRepository.getPaymentMethod().paymentInstrumentData?.paymentMethodType
    )

    private fun getPaymentMethodTypeString() =
        paymentMethodRepository.getPaymentMethod().paymentInstrumentData?.paymentMethodType

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
        const val CLIENT_TOKEN_INTENT_SUFFIX = "_REDIRECTION"
    }
}
