package io.primer.android.components.presentation.paymentMethods.formWithRedirect.redirect.webRedirect.delegate

import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class WebRedirectDelegate {

    fun errors(): Flow<PrimerError> = callbackFlow {
        val subscription: EventBus.SubscriptionHandle = EventBus.subscribe { checkoutEvent ->
            when (checkoutEvent) {
                is CheckoutEvent.CheckoutError -> {
                    trySendImpl(checkoutEvent.error)
                }

                is CheckoutEvent.CheckoutPaymentError -> {
                    trySendImpl(checkoutEvent.error)
                }

                else -> {
                    // no-op
                }
            }
        }
        awaitClose {
            subscription.unregister()
        }
    }

    private fun ProducerScope<PrimerError>.trySendImpl(primerError: PrimerError) {
        if (primerError !is PaymentMethodError.PaymentMethodCancelledError) {
            trySend(primerError)
        }
    }

    fun steps(): Flow<WebRedirectStep> = callbackFlow {
        val subscription: EventBus.SubscriptionHandle = EventBus.subscribe { checkoutEvent ->
            when (checkoutEvent) {
                is CheckoutEvent.PaymentMethodPresented -> {
                    trySend(WebRedirectStep.Loaded)
                }

                is CheckoutEvent.PaymentSuccess -> {
                    trySend(WebRedirectStep.Success)
                }

                is CheckoutEvent.CheckoutError -> {
                    trySendDismissedStep(checkoutEvent.error)
                }

                is CheckoutEvent.CheckoutPaymentError -> {
                    trySendDismissedStep(checkoutEvent.error)
                }

                else -> {
                    // no-op
                }
            }
        }
        awaitClose {
            subscription.unregister()
        }
    }

    private fun ProducerScope<WebRedirectStep>.trySendDismissedStep(primerError: PrimerError) {
        if (primerError is PaymentMethodError.PaymentMethodCancelledError) {
            trySend(WebRedirectStep.Dismissed)
        }
    }
}
