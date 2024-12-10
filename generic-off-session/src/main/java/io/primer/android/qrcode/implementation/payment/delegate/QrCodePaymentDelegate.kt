package io.primer.android.qrcode.implementation.payment.delegate

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.qrcode.QrCodeCheckoutAdditionalInfo
import io.primer.android.qrcode.implementation.payment.resume.handler.QrCodeResumeHandler

internal class QrCodePaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    private val additionalInfoHandler: CheckoutAdditionalInfoHandler,
    errorHandler: CheckoutErrorHandler,
    private val pollingStartHandler: PollingStartHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: QrCodeResumeHandler,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
) : PaymentMethodPaymentDelegate(
    paymentMethodTokenHandler,
    resumePaymentHandler,
    successHandler,
    errorHandler,
    baseErrorResolver
) {

    override suspend fun handleNewClientToken(clientToken: String, payment: Payment?): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken)
            .mapSuspendCatching { decision ->
                val paymentMethodType = tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType
                pollingStartHandler.handle(
                    PollingStartHandler.PollingStartData(
                        statusUrl = decision.statusUrl,
                        paymentMethodType = requireNotNull(paymentMethodType)
                    )
                )

                val additionalInfo = QrCodeCheckoutAdditionalInfo(
                    statusUrl = decision.statusUrl,
                    expiresAt = decision.expiresAt,
                    qrCodeUrl = decision.qrCodeUrl,
                    qrCodeBase64 = decision.qrCodeBase64,
                    paymentMethodType = paymentMethodType
                )

                additionalInfoHandler.handle(additionalInfo)
            }
    }
}
