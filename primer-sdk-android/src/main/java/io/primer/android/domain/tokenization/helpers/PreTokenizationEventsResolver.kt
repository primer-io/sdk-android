package io.primer.android.domain.tokenization.helpers

import io.primer.android.completion.PaymentCreationDecisionHandler
import io.primer.android.domain.tokenization.models.PaymentMethodData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PaymentHandling
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethodType
import io.primer.android.ui.fragments.ErrorType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class PreTokenizationEventsResolver(
    private val config: PrimerConfig,
    private val eventDispatcher: EventDispatcher,
) {

    suspend fun resolve(paymentMethodType: PrimerPaymentMethodType) {
        suspendCancellableCoroutine<Unit> { continuation ->
            when {
                config.intent.paymentMethodIntent.isVault ||
                    config.settings.options.paymentHandling == PaymentHandling.MANUAL -> {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.TokenizationStarted(
                            paymentMethodType
                        )
                    )
                    continuation.resume(Unit)
                }
                config.settings.options.paymentHandling == PaymentHandling.AUTO -> {
                    val handler = object : PaymentCreationDecisionHandler {
                        override fun continuePaymentCreation() {
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
                    if (config.fromHUC) {
                        eventDispatcher.dispatchEvent(
                            CheckoutEvent.PaymentCreateStartedHUC(
                                PaymentMethodData(paymentMethodType),
                                handler
                            )
                        )
                    } else {
                        eventDispatcher.dispatchEvent(
                            CheckoutEvent.PaymentCreateStarted(
                                PaymentMethodData(paymentMethodType),
                                handler
                            )
                        )
                    }
                }
            }
        }
    }
}
