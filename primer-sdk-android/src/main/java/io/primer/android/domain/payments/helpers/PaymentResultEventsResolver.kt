package io.primer.android.domain.payments.helpers

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.create.model.toPrimerCheckoutData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher

internal class PaymentResultEventsResolver(private val eventDispatcher: EventDispatcher) {

    fun resolve(paymentResult: PaymentResult, resumeHandler: PrimerResumeDecisionHandler) {
        when (paymentResult.paymentStatus) {
            PaymentStatus.PENDING -> {
                resumeHandler.continueWithNewClientToken(paymentResult.clientToken.orEmpty())
            }
            PaymentStatus.FAILED -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.CheckoutPaymentError(
                        PaymentError.PaymentFailedError,
                        PrimerCheckoutData(paymentResult.payment),
                        object :
                            PrimerErrorDecisionHandler {
                            override fun showErrorMessage(errorMessage: String?) {
                                resumeHandler.handleFailure(errorMessage)
                            }
                        }
                    )
                )
            }
            else -> completePaymentWithResult(paymentResult, resumeHandler)
        }
    }

    private fun completePaymentWithResult(
        paymentResult: PaymentResult,
        resumeHandler: PrimerResumeDecisionHandler
    ) {
        eventDispatcher.dispatchEvent(
            CheckoutEvent.PaymentSuccess(
                paymentResult.toPrimerCheckoutData(),
            )
        )
        resumeHandler.handleSuccess()
    }
}
