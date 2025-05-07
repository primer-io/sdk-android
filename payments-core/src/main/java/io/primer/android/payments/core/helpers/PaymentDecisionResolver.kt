package io.primer.android.payments.core.helpers

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.create.data.model.CheckoutOutcome
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal class PaymentDecisionResolver(
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val logReporter: LogReporter,
) {
    fun resolve(paymentResult: PaymentResult): PaymentDecision {
        logReporter.info("Received new payment status: ${paymentResult.paymentStatus}.")
        return when (paymentResult.checkoutOutcome) {
            CheckoutOutcome.CHECKOUT_COMPLETE -> PaymentDecision.Success(payment = paymentResult.payment)
            CheckoutOutcome.CHECKOUT_FAILURE -> PaymentDecision.Error(
                error = paymentResult.toError(),
                payment = paymentResult.payment,
            )
            else -> paymentResult.toPaymentDecision()
        }
    }

    private fun PaymentResult.toPaymentDecision() = when {
        paymentStatus == PaymentStatus.PENDING &&
            showSuccessCheckoutOnPendingPayment.not() -> {
            logReporter.debug(
                "Handling required action: ${requiredActionName?.name}" +
                    " for payment id: ${payment.id}",
            )
            PaymentDecision.Pending(
                clientToken = clientToken.orEmpty(),
                payment = payment,
            )
        }

        paymentStatus == PaymentStatus.FAILED -> {
            PaymentDecision.Error(
                error = toError(),
                payment = payment,
            )
        }
        else -> PaymentDecision.Success(payment = payment)
    }

    private fun PaymentResult.toError(): PaymentError.PaymentFailedError {
        return PaymentError.PaymentFailedError(
            paymentId = payment.id,
            paymentStatus = paymentStatus,
            paymentMethodType = tokenizedPaymentMethodRepository.getPaymentMethod()
                .paymentMethodType.orEmpty(),
        )
    }
}
