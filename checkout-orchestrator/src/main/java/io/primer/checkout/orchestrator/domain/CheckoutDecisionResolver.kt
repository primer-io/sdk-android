package io.primer.checkout.orchestrator.domain

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.checkout.orchestrator.domain.model.CheckoutDecision
import io.primer.statetransport.domain.model.CheckoutOutcome
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.PaymentStatus

internal class CheckoutDecisionResolver(
    private val logReporter: LogReporter,
) {
    fun resolve(
        end: ClientInstructions.End,
        paymentMethodType: String,
    ): CheckoutDecision {
        val payment = end.payment?.let { Payment(id = it.id, orderId = it.orderId) }
        logReporter.info("Checkout ended with outcome: ${end.checkoutOutcome}.")

        return when (end.checkoutOutcome) {
            CheckoutOutcome.CHECKOUT_COMPLETE -> {
                CheckoutDecision.Success(payment = payment)
            }

            CheckoutOutcome.CHECKOUT_FAILURE -> {
                CheckoutDecision.Failure(
                    error = PaymentError.PaymentFailedError(
                        paymentId = requireNotNull(payment?.id),
                        paymentStatus = io.primer.android.payments.core.create.data.model.PaymentStatus.valueOf(
                            requireNotNull(
                                end.payment?.status?.name,
                            ),
                        ),
                        paymentMethodType = paymentMethodType,
                    ),
                    payment = payment,
                )
            }

            else -> resolveFromPaymentStatus(end, payment, paymentMethodType)
        }
    }

    private fun resolveFromPaymentStatus(
        end: ClientInstructions.End,
        payment: Payment?,
        paymentMethodType: String,
    ): CheckoutDecision = when (end.payment?.status) {
        PaymentStatus.FAILED -> CheckoutDecision.Failure(
            error = PaymentError.PaymentFailedError(
                paymentId = requireNotNull(payment?.id),
                paymentStatus = io.primer.android.payments.core.create.data.model.PaymentStatus.valueOf(
                    requireNotNull(
                        end.payment?.status?.name,
                    ),
                ),
                paymentMethodType = paymentMethodType,
            ),
            payment = payment,
        )

        else -> CheckoutDecision.Success(payment = payment)
    }
}
