package io.primer.android.domain.payments.helpers

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.create.model.toPrimerCheckoutData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository

internal class PaymentResultEventsResolver(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val eventDispatcher: EventDispatcher,
    private val logReporter: LogReporter
) {

    fun resolve(paymentResult: PaymentResult, resumeHandler: PrimerResumeDecisionHandler) {
        logReporter.info("Received new payment status: ${paymentResult.paymentStatus}.")
        when (paymentResult.paymentStatus) {
            PaymentStatus.PENDING -> {
                logReporter.debug(
                    "Handling required action: ${paymentResult.requiredActionName?.name}" +
                        " for payment id: ${paymentResult.payment.id}"
                )
                resumeHandler.continueWithNewClientToken(paymentResult.clientToken.orEmpty())
            }

            PaymentStatus.FAILED -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.CheckoutPaymentError(
                        PaymentError.PaymentFailedError(
                            paymentResult.payment.id,
                            paymentResult.paymentStatus,
                            paymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty()
                        ),
                        PrimerCheckoutData(paymentResult.payment),
                        object : PrimerErrorDecisionHandler {
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
                paymentResult.toPrimerCheckoutData()
            )
        )
        resumeHandler.handleSuccess()
    }
}
