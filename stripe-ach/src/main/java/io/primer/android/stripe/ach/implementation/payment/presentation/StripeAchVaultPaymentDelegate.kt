package io.primer.android.stripe.ach.implementation.payment.presentation

import io.primer.android.core.extensions.toIso8601String
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.payment.confirmation.presentation.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchVaultResumeDecisionHandler
import java.util.Date

@Suppress("LongParameterList")
internal class StripeAchVaultPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    private val successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeDecisionHandler: StripeAchVaultResumeDecisionHandler,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val pendingResumeHandler: PendingResumeHandler,
    private val manualFlowSuccessHandler: ManualFlowSuccessHandler,
    private val paymentResultRepository: PaymentResultRepository,
    private val config: PrimerConfig,
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
    ): Result<Unit> =
        resumeDecisionHandler.continueWithNewClientToken(clientToken)
            .mapCatching {
                val date = Date()
                completeStripeAchPaymentSessionDelegate.invoke(
                    completeUrl = it.sdkCompleteUrl,
                    paymentMethodId = null,
                    mandateTimestamp = date,
                )
                date.toIso8601String()
            }
            .mapCatching { date ->
                when (config.settings.paymentHandling) {
                    PrimerPaymentHandling.MANUAL -> {
                        pendingResumeHandler.handle(additionalInfo = AchAdditionalInfo.MandateAccepted(date))
                        manualFlowSuccessHandler.handle()
                    }

                    PrimerPaymentHandling.AUTO ->
                        successHandler.handle(
                            payment = paymentResultRepository.getPaymentResult().payment,
                            additionalInfo = null,
                        )
                }
            }
            .onFailure { handleError(it) }
            .map { /* no-op */ }
}
