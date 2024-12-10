package io.primer.android.payments.core.helpers

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal class PaymentDecisionResolver(
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val logReporter: LogReporter
) {

    fun resolve(
        paymentResult: PaymentResult
    ): PaymentDecision {
        logReporter.info("Received new payment status: ${paymentResult.paymentStatus}.")
        return when {
            paymentResult.paymentStatus == PaymentStatus.PENDING &&
                paymentResult.showSuccessCheckoutOnPendingPayment.not() -> {
                logReporter.debug(
                    "Handling required action: ${paymentResult.requiredActionName?.name}" +
                        " for payment id: ${paymentResult.payment.id}"
                )
                PaymentDecision.Pending(
                    clientToken = paymentResult.clientToken.orEmpty(),
                    payment = paymentResult.payment
                )
            }

            paymentResult.paymentStatus == PaymentStatus.FAILED -> {
                PaymentDecision.Error(
                    error = PaymentError.PaymentFailedError(
                        paymentId = paymentResult.payment.id,
                        paymentStatus = paymentResult.paymentStatus,
                        paymentMethodType =
                        tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty()
                    ),
                    payment = paymentResult.payment
                )
            }

            else -> PaymentDecision.Success(payment = paymentResult.payment)
        }
    }
}
