package io.primer.android.domain.tokenization.helpers

import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.fragments.ErrorType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class PreTokenizationEventsResolver(
    private val config: PrimerConfig,
    private val eventDispatcher: EventDispatcher,
) {

    suspend fun resolve(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT
    ) {
        suspendCancellableCoroutine { continuation ->
            when {
                config.intent.paymentMethodIntent.isVault ||
                    sessionIntent == PrimerSessionIntent.VAULT ||
                    config.settings.paymentHandling == PrimerPaymentHandling.MANUAL -> {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.TokenizationStarted(
                            paymentMethodType
                        )
                    )
                    continuation.resume(Unit)
                }

                config.settings.paymentHandling == PrimerPaymentHandling.AUTO -> {
                    val handler = object : PrimerPaymentCreationDecisionHandler {
                        override fun continuePaymentCreation() {
                            eventDispatcher.dispatchEvent(
                                CheckoutEvent.TokenizationStarted(
                                    paymentMethodType
                                )
                            )
                            continuation.resume(Unit)
                        }

                        override fun abortPaymentCreation(errorMessage: String?) {
                            eventDispatcher.dispatchEvent(
                                CheckoutEvent.ShowError(
                                    errorType = ErrorType.PAYMENT_FAILED,
                                    message = errorMessage
                                )
                            )
                        }
                    }
                    if (config.settings.fromHUC) {
                        eventDispatcher.dispatchEvent(
                            CheckoutEvent.PaymentCreateStartedHUC(
                                PrimerPaymentMethodData(paymentMethodType),
                                handler
                            )
                        )
                    } else {
                        eventDispatcher.dispatchEvent(
                            CheckoutEvent.PaymentCreateStarted(
                                PrimerPaymentMethodData(paymentMethodType),
                                handler
                            )
                        )
                    }
                }
            }
        }
    }
}
