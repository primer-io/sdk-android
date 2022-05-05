package io.primer.android.domain.payments.helpers

import io.primer.android.completion.CheckoutErrorHandler
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.domain.CheckoutData
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher

internal class PaymentResultEventsResolver(private val eventDispatcher: EventDispatcher) {

    fun resolve(paymentResult: PaymentResult, resumeHandler: ResumeDecisionHandler) {
        when (paymentResult.paymentStatus) {
            PaymentStatus.PENDING -> resumeHandler.handleNewClientToken(
                paymentResult.clientToken.orEmpty()
            )
            PaymentStatus.FAILED -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.CheckoutPaymentError(
                        PaymentError.PaymentFailedError,
                        CheckoutData(paymentResult.payment),
                        object :
                            CheckoutErrorHandler {
                            override fun showErrorMessage(message: String?) {
                                resumeHandler.handleError(message)
                            }
                        }
                    )
                )
            }
            else -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.PaymentSuccess(
                        CheckoutData(
                            paymentResult.payment
                        )
                    )
                )
                resumeHandler.handleSuccess()
            }
        }
    }
}
