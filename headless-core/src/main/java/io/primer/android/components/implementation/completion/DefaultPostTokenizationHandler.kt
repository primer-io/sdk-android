package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.create.domain.handler.PostTokenizationHandler
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DefaultPostTokenizationHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) : PostTokenizationHandler {
    override suspend fun handle(paymentMethodTokenData: PrimerPaymentMethodTokenData): Result<PaymentDecision> =
        suspendCoroutine { continuation ->
            coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
                val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener
                analyticsRepository.addEvent(
                    SdkFunctionParams(
                        HeadlessUniversalCheckoutAnalyticsConstants.ON_TOKENIZE_SUCCESS
                    )
                )
                val handler = createManualDecisionHandler(continuation)
                checkoutListener?.onTokenizeSuccess(
                    paymentMethodTokenData = paymentMethodTokenData,
                    decisionHandler = handler
                )
            }
        }

    private fun createManualDecisionHandler(continuation: Continuation<Result<PaymentDecision>>):
        PrimerHeadlessUniversalCheckoutResumeDecisionHandler {
        return object : PrimerHeadlessUniversalCheckoutResumeDecisionHandler {
            override fun continueWithNewClientToken(clientToken: String) {
                continuation.resume(Result.success(PaymentDecision.Pending(clientToken = clientToken, payment = null)))
            }
        }
    }
}
