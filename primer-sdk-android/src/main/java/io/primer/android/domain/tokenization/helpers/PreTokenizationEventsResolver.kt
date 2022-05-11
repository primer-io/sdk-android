package io.primer.android.domain.tokenization.helpers

import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PrimerPaymentHandling
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
                    config.settings.options.paymentHandling == PrimerPaymentHandling.MANUAL -> {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.TokenizationStarted(
                            paymentMethodType
                        )
                    )
                    continuation.resume(Unit)
                }
                config.settings.options.paymentHandling == PrimerPaymentHandling.AUTO -> {
                    val handler = object : PrimerPaymentCreationDecisionHandler {
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
