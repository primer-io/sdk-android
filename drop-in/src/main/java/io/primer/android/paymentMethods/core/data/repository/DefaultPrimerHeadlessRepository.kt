package io.primer.android.paymentMethods.core.data.repository

import android.content.Context
import io.primer.android.Primer
import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.paymentMethods.core.domain.events.PrimerEvent
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge

internal class DefaultPrimerHeadlessRepository(
    private val context: Context,
    private val config: PrimerConfig,
) : PrimerHeadlessRepository {
    private val headlessUniversalCheckout = PrimerHeadlessUniversalCheckout.current

    private val externalEvents = MutableSharedFlow<PrimerEvent>(replay = 1)

    override val events: Flow<PrimerEvent> =
        merge(
            externalEvents,
            callbackFlow {
                headlessUniversalCheckout.setCheckoutListener(
                    object : PrimerHeadlessUniversalCheckoutListener {
                        override fun onAvailablePaymentMethodsLoaded(
                            paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
                        ) {
                            trySend(
                                PrimerEvent.AvailablePaymentMethodsLoaded(
                                    PrimerEventsInteractor.PaymentMethodsHolder(
                                        paymentMethods = paymentMethods,
                                    ),
                                ),
                            )
                        }

                        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
                            handleSuccessImpl(checkoutData)
                            Primer.current.listener?.onCheckoutCompleted(checkoutData)
                        }

                        override fun onFailed(
                            error: PrimerError,
                            checkoutData: PrimerCheckoutData?,
                        ) {
                            Primer.current.listener?.onFailed(
                                error = error,
                                checkoutData = checkoutData,
                                errorHandler =
                                    object : PrimerErrorDecisionHandler {
                                        override fun showErrorMessage(errorMessage: String?) {
                                            handleFailureImpl(
                                                message = errorMessage,
                                                isCancellation = error is PaymentMethodCancelledError,
                                            )
                                        }
                                    },
                            )
                        }

                        override fun onFailed(error: PrimerError) {
                            Primer.current.listener?.onFailed(
                                error = error,
                                errorHandler =
                                    object : PrimerErrorDecisionHandler {
                                        override fun showErrorMessage(errorMessage: String?) {
                                            handleFailureImpl(
                                                message = errorMessage,
                                                isCancellation = error is PaymentMethodCancelledError,
                                            )
                                        }
                                    },
                            )
                        }

                        override fun onTokenizationStarted(paymentMethodType: String) {
                            externalEvents.tryEmit(PrimerEvent.DisableDismiss)
                        }

                        override fun onTokenizeSuccess(
                            paymentMethodTokenData: PrimerPaymentMethodTokenData,
                            decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler,
                        ) {
                            Primer.current.listener?.onTokenizeSuccess(
                                paymentMethodTokenData,
                                decisionHandler = createDecisionHandler(decisionHandler),
                            )
                        }

                        override fun onBeforePaymentCreated(
                            paymentMethodData: PrimerPaymentMethodData,
                            createPaymentHandler: PrimerPaymentCreationDecisionHandler,
                        ) {
                            externalEvents.tryEmit(PrimerEvent.DisableDismiss)
                            Primer.current.listener?.onBeforePaymentCreated(paymentMethodData, createPaymentHandler)
                        }

                        override fun onBeforeClientSessionUpdated() {
                            Primer.current.listener?.onBeforeClientSessionUpdated()
                        }

                        override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
                            Primer.current.listener?.onClientSessionUpdated(clientSession)
                        }

                        override fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) {
                            Primer.current.listener?.onResumePending(additionalInfo)
                        }

                        override fun onCheckoutResume(
                            resumeToken: String,
                            decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler,
                        ) {
                            Primer.current.listener?.onResumeSuccess(
                                resumeToken,
                                createDecisionHandler(decisionHandler),
                            )
                        }

                        override fun onCheckoutAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) {
                            Primer.current.listener?.onAdditionalInfoReceived(additionalInfo)
                        }

                        private fun createDecisionHandler(
                            decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler,
                        ) = object : PrimerResumeDecisionHandler {
                            override fun handleFailure(message: String?) {
                                handleFailureImpl(message)
                            }

                            override fun handleSuccess() {
                                handleSuccessImpl(checkoutData = null)
                            }

                            override fun continueWithNewClientToken(clientToken: String) {
                                decisionHandler.continueWithNewClientToken(clientToken)
                            }
                        }

                        fun handleFailureImpl(
                            message: String?,
                            isCancellation: Boolean = false,
                        ) {
                            if (config.settings.uiOptions.isSuccessScreenEnabled) {
                                val errorType =
                                    when (config.intent.paymentMethodIntent) {
                                        PrimerSessionIntent.CHECKOUT ->
                                            if (isCancellation) {
                                                ErrorType.PAYMENT_CANCELLED
                                            } else {
                                                ErrorType.PAYMENT_FAILED
                                            }

                                        PrimerSessionIntent.VAULT -> ErrorType.VAULT_TOKENIZATION_FAILED
                                    }
                                trySend(
                                    PrimerEvent.CheckoutFailed(
                                        errorMessage = message,
                                        errorType = errorType,
                                    ),
                                )
                            } else {
                                trySend(PrimerEvent.Dismiss)
                            }
                        }
                    },
                )

                awaitClose { }
            },
        )

    override fun start(clientToken: String) {
        headlessUniversalCheckout.start(context, clientToken)
    }

    override fun cleanup() {
        headlessUniversalCheckout.cleanup()
    }

    override suspend fun handleManualFlowSuccess(additionalInfo: PrimerCheckoutAdditionalInfo?) {
        handleSuccessImpl(
            checkoutData =
                PrimerCheckoutData(
                    payment = Payment.undefined,
                    additionalInfo = additionalInfo,
                ),
        )
    }

    private fun handleSuccessImpl(checkoutData: PrimerCheckoutData?) {
        if (config.settings.uiOptions.isSuccessScreenEnabled) {
            val successType =
                when (config.intent.paymentMethodIntent) {
                    PrimerSessionIntent.CHECKOUT -> SuccessType.PAYMENT_SUCCESS
                    PrimerSessionIntent.VAULT -> SuccessType.VAULT_TOKENIZATION_SUCCESS
                }
            externalEvents.tryEmit(
                PrimerEvent.CheckoutCompleted(checkoutData, successType = successType),
            )
        } else {
            externalEvents.tryEmit(PrimerEvent.Dismiss)
        }
    }
}
