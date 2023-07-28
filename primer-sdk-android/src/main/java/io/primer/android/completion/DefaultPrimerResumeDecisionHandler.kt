package io.primer.android.completion

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutQRCodeInfo
import io.primer.android.domain.payments.additionalInfo.RetailOutletsCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
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
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val retailerOutletRepository: RetailOutletRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PrimerHeadlessUniversalCheckoutResumeDecisionHandler, PrimerResumeDecisionHandler {

    private var handlerUsed = false

    override fun handleFailure(message: String?) = callIfNotHandled {
        addAnalyticsEvent("handleFailure")
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED, message = message)
        )
    }

    override fun handleSuccess() = callIfNotHandled {
        addAnalyticsEvent("handleSuccess")
        val successType = when (config.intent.paymentMethodIntent) {
            PrimerSessionIntent.CHECKOUT -> SuccessType.PAYMENT_SUCCESS
            PrimerSessionIntent.VAULT -> SuccessType.VAULT_TOKENIZATION_SUCCESS
        }
        eventDispatcher.dispatchEvent(
            CheckoutEvent.ShowSuccess(successType = successType)
        )
    }

    override fun continueWithNewClientToken(clientToken: String) = callIfNotHandled {
        addAnalyticsEvent("handleNewClientToken")
        CoroutineScope(dispatcher).launch {
            validateClientToken(clientToken).catch { e ->
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

    private suspend fun handlePaymentMethodData(clientToken: String) {
        val clientTokenConfig = ClientToken.fromString(clientToken)
        paymentMethodDescriptorsRepository.resolvePaymentMethodDescriptors()
            .mapLatest { descriptors ->
                val descriptor = descriptors.firstOrNull { descriptor ->
                    descriptor.config.type ==
                        paymentMethodRepository.getPaymentMethod().paymentMethodType
                }

                val additionalInfoResolver = descriptor?.additionalInfoResolver
                when (additionalInfoResolver) {
                    is RetailOutletsCheckoutAdditionalInfoResolver -> {
                        additionalInfoResolver.retailerName = retailerOutletRepository
                            .getSelectedRetailOutlet()?.name
                    }
                }

                additionalInfoResolver?.resolve(clientTokenConfig)?.let { data ->
                    when (data) {
                        is PrimerCheckoutQRCodeInfo -> {
                            eventDispatcher.dispatchEvent(
                                CheckoutEvent.OnAdditionalInfoReceived(
                                    data
                                )
                            )
                        }
                        else -> {
                            if (config.settings.paymentHandling == PrimerPaymentHandling.MANUAL) {
                                eventDispatcher.dispatchEvent(CheckoutEvent.ResumePending(data))
                            } else {
                                val paymentResult = paymentResultRepository.getPaymentResult()
                                eventDispatcher.dispatchEvent(
                                    CheckoutEvent.PaymentSuccess(
                                        PrimerCheckoutData(paymentResult.payment, data)
                                    )
                                )
                            }
                        }
                    }
                }
            }.collect()
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

    private fun validateClientToken(clientToken: String): Flow<Boolean> {
        return when (config.settings.paymentHandling) {
            PrimerPaymentHandling.MANUAL -> validationTokenRepository.validate(clientToken)
            PrimerPaymentHandling.AUTO -> flowOf(true)
        }
    }

    protected companion object {

        const val HANDLER_USED_ERROR = "ResumeHandler can be used only once."
        const val RESUME_INTENT_ERROR = "Unexpected client token intent"
        const val CLIENT_TOKEN_INTENT_SUFFIX = "_REDIRECTION"
    }
}
