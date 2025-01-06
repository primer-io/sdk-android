package io.primer.android.phoneNumber.implementation.payment.delegate

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.phoneNumber.implementation.payment.resume.handler.PhoneNumberResumeHandler

internal class PhoneNumberPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    private val pollingStartHandler: PollingStartHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: PhoneNumberResumeHandler,
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
        return resumeHandler.continueWithNewClientToken(clientToken)
            .mapSuspendCatching { decision ->
                val paymentMethodType = tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType
                pollingStartHandler.handle(
                    PollingStartHandler.PollingStartData(
                        statusUrl = decision.statusUrl,
                        paymentMethodType = requireNotNull(paymentMethodType),
                    ),
                )
            }
    }
}
