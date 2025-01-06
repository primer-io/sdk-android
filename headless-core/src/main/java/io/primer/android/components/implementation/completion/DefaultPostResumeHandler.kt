package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.resume.domain.handler.PostResumeHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class DefaultPostResumeHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : PostResumeHandler {
    override suspend fun handle(resumeToken: String): Result<PaymentDecision> {
        return suspendCoroutine { continuation ->
            analyticsRepository.addEvent(
                SdkFunctionParams(
                    HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_RESUME,
                ),
            )
            coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
                val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener

                val handler = createManualDecisionHandler(continuation)
                checkoutListener?.onCheckoutResume(
                    resumeToken = resumeToken,
                    decisionHandler = handler,
                )
            }
        }
    }

    private fun createManualDecisionHandler(
        continuation: Continuation<Result<PaymentDecision>>,
    ): PrimerHeadlessUniversalCheckoutResumeDecisionHandler {
        return object : PrimerHeadlessUniversalCheckoutResumeDecisionHandler {
            override fun continueWithNewClientToken(clientToken: String) {
                continuation.resume(Result.success(PaymentDecision.Pending(clientToken, null)))
            }
        }
    }
}
