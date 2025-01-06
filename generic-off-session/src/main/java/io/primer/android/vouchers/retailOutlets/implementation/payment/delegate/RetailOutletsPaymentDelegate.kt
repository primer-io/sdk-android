package io.primer.android.vouchers.retailOutlets.implementation.payment.delegate

import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler.RetailOutletsResumeHandler

internal class RetailOutletsPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: RetailOutletsResumeHandler,
) : PaymentMethodPaymentDelegate(
        paymentMethodTokenHandler,
        resumePaymentHandler,
        successHandler,
        errorHandler,
        baseErrorResolver,
    ) {
    override suspend fun handleNewClientToken(
        clientToken: String,
        payment: Payment?,
    ): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken).map {
            // no-op
        }
    }
}
