package io.primer.android.vouchers.multibanco.implementation.payment.delegate

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import io.primer.android.vouchers.multibanco.implementation.payment.resume.handler.MultibancoResumeHandler

internal class MultibancoPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    private val config: PrimerConfig,
    private val successHandler: CheckoutSuccessHandler,
    private val manualFlowSuccessHandler: ManualFlowSuccessHandler,
    private val pendingResumeHandler: PendingResumeHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: MultibancoResumeHandler
) : PaymentMethodPaymentDelegate(
    paymentMethodTokenHandler,
    resumePaymentHandler,
    successHandler,
    errorHandler,
    baseErrorResolver
) {
    override suspend fun handleNewClientToken(clientToken: String, payment: Payment?): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken).mapSuspendCatching { decision ->
            val additionalInfo = MultibancoCheckoutAdditionalInfo(
                expiresAt = decision.expiresAt,
                reference = decision.reference,
                entity = decision.entity
            )

            when (config.settings.paymentHandling) {
                PrimerPaymentHandling.MANUAL -> {
                    pendingResumeHandler.handle(additionalInfo = additionalInfo)
                    manualFlowSuccessHandler.handle(additionalInfo = additionalInfo)
                }
                PrimerPaymentHandling.AUTO -> successHandler.handle(
                    payment = requireNotNull(payment),
                    additionalInfo = additionalInfo
                )
            }
        }
    }
}
