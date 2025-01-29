package io.primer.android.sandboxProcessor.implementation.payment.delegate

import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.data.exception.UnhandledPaymentPendingStateException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal class SandboxProcessorPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
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
        return Result.failure(
            UnhandledPaymentPendingStateException(
                tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty(),
            ),
        )
    }
}
