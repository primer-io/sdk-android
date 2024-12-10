package io.primer.android.stripe.ach.implementation.payment.presentation

import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchDecision
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchResumeDecisionHandler

internal class StripeAchPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeDecisionHandler: StripeAchResumeDecisionHandler
) : PaymentMethodPaymentDelegate(
    paymentMethodTokenHandler,
    resumePaymentHandler,
    successHandler,
    errorHandler,
    baseErrorResolver
) {
    var lastDecision: StripeAchDecision? = null
        private set

    override suspend fun handleNewClientToken(clientToken: String, payment: Payment?): Result<Unit> =
        resumeDecisionHandler.continueWithNewClientToken(clientToken)
            .onSuccess { decision -> lastDecision = decision }
            .map { /* no-op */ }
}
