package io.primer.android.stripe.ach.implementation.selection.presentation

import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.mandate.presentation.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.stripe.ach.implementation.payment.confirmation.presentation.CompleteStripeAchPaymentSessionDelegate
import kotlinx.coroutines.CompletableDeferred
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

/**
 * Handles Stripe ACH bank selection and mandate.
 */
internal class StripeAchBankFlowDelegate(
    private val stripeAchBankSelectionHandler: StripeAchBankSelectionHandler,
    private val checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
    private val stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val paymentResultRepository: PaymentResultRepository,
) {
    suspend fun handle(
        clientSecret: String,
        paymentIntentId: String,
        sdkCompleteUrl: String,
    ): Result<StripeAchBankFlowResult> =
        stripeAchBankSelectionHandler.fetchSelectedBankId(clientSecret = clientSecret)
            .mapCatching { paymentMethodId ->
                val completableDeferred = CompletableDeferred<StripeAchBankFlowResult>()
                checkoutAdditionalInfoHandler.handle(
                    checkoutAdditionalInfo =
                    createDisplayMandateAdditionalInfo(
                        paymentMethodId = paymentMethodId,
                        paymentIntentId = paymentIntentId,
                        sdkCompleteUrl = sdkCompleteUrl,
                        completableDeferred = completableDeferred,
                    ),
                )
                completableDeferred.await()
            }

    private fun createDisplayMandateAdditionalInfo(
        paymentMethodId: String,
        paymentIntentId: String,
        sdkCompleteUrl: String,
        completableDeferred: CompletableDeferred<StripeAchBankFlowResult>,
    ): PrimerCheckoutAdditionalInfo =
        AchAdditionalInfo.DisplayMandate(
            onAcceptMandate = {
                runCatching {
                    val date = Date()
                    stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                        stripePaymentIntentId = paymentIntentId,
                        date = date,
                    )
                    date
                }.flatMap { date ->
                    try {
                        completeStripeAchPaymentSessionDelegate.invoke(
                            completeUrl = sdkCompleteUrl,
                            paymentMethodId = paymentMethodId,
                            mandateTimestamp = date,
                        ).map { date }
                    } catch (e: CancellationException) {
                        completableDeferred.completeExceptionally(e)
                        throw e
                    }
                }.onSuccess { date ->
                    completableDeferred.complete(
                        StripeAchBankFlowResult(
                            payment = runCatching { paymentResultRepository.getPaymentResult().payment }.getOrNull(),
                            mandateTimestamp = date.toIso8601String(),
                        ),
                    )
                }.onFailure {
                    completableDeferred.completeExceptionally(it)
                }
            },
            onDeclineMandate = {
                completableDeferred.completeExceptionally(
                    PaymentMethodCancelledException(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    ),
                )
            },
        )

    data class StripeAchBankFlowResult(val payment: Payment?, val mandateTimestamp: String)
}
