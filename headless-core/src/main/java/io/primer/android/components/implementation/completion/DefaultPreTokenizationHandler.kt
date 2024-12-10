package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun interface PreTokenizationHandlerStrategy {
    suspend fun handle(
        paymentMethodType: String
    ): Result<Unit>
}

internal class AutoPreTokenizationHandlerStrategy(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) : PreTokenizationHandlerStrategy {

    override suspend fun handle(
        paymentMethodType: String
    ): Result<Unit> {
        return suspendCoroutine { continuation ->
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener

            val handler = object : PrimerPaymentCreationDecisionHandler {
                override fun continuePaymentCreation() {
                    analyticsRepository.addEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_TOKENIZATION_STARTED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                                    to paymentMethodType
                            )
                        )
                    )
                    checkoutListener?.onTokenizationStarted(paymentMethodType = paymentMethodType)
                    continuation.resume(Result.success(Unit))
                }

                override fun abortPaymentCreation(errorMessage: String?) {
                    continuation.resume(Result.failure(IllegalStateException(errorMessage)))
                }
            }
            coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
                analyticsRepository.addEvent(
                    SdkFunctionParams(
                        HeadlessUniversalCheckoutAnalyticsConstants.ON_BEFORE_PAYMENT_COMPLETED
                    )
                )
                checkoutListener?.onBeforePaymentCreated(
                    paymentMethodData = PrimerPaymentMethodData(paymentMethodType),
                    createPaymentHandler = handler
                )
            }
        }
    }
}

internal class ManualPreTokenizationHandlerStrategy(
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) : PreTokenizationHandlerStrategy {
    override suspend fun handle(
        paymentMethodType: String
    ): Result<Unit> = suspendCoroutine { continuation ->
        val checkoutListener =
            PrimerHeadlessUniversalCheckout.instance.checkoutListener

        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            checkoutListener?.onTokenizationStarted(paymentMethodType)
        }
        continuation.resume(Result.success(Unit))
    }
}

class DefaultPreTokenizationHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val config: PrimerConfig
) : PreTokenizationHandler {

    private enum class PaymentHandlingStrategy {
        AUTO,
        MANUAL,
        VAULT
    }

    private val strategies: Map<PaymentHandlingStrategy, PreTokenizationHandlerStrategy> = mapOf(
        PaymentHandlingStrategy.AUTO to AutoPreTokenizationHandlerStrategy(analyticsRepository = analyticsRepository),
        PaymentHandlingStrategy.MANUAL to ManualPreTokenizationHandlerStrategy(),
        PaymentHandlingStrategy.VAULT to ManualPreTokenizationHandlerStrategy()
    )

    override suspend fun handle(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent
    ): Result<Unit> {
        val paymentHandlingStrategy = when {
            sessionIntent == PrimerSessionIntent.VAULT -> PaymentHandlingStrategy.VAULT
            config.settings.paymentHandling == PrimerPaymentHandling.MANUAL -> PaymentHandlingStrategy.MANUAL
            else -> PaymentHandlingStrategy.AUTO
        }
        return strategies[paymentHandlingStrategy]?.handle(paymentMethodType = paymentMethodType)
            ?: error("Unregistered pre tokenization strategy for $paymentHandlingStrategy ")
    }
}
