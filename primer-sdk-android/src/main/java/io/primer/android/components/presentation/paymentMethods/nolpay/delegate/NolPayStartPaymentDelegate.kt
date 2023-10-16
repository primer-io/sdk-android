package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.AsyncPaymentMethodConfigInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayCompletePaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequiredActionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCompletePaymentParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequestPaymentParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequiredAction
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.None
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.mapSuspendCatching
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Suppress("LongParameterList")
internal class NolPayStartPaymentDelegate(
    private val asyncPaymentMethodConfigInteractor: AsyncPaymentMethodConfigInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val requestPaymentInteractor: NolPayRequestPaymentInteractor,
    private val requiredActionInteractor: NolPayRequiredActionInteractor,
    private val completePaymentInteractor: NolPayCompletePaymentInteractor,
    sdkInitInteractor: NolPaySdkInitInteractor,
    analyticsInteractor: AnalyticsInteractor
) : BaseNolPayDelegate(sdkInitInteractor, analyticsInteractor) {

    private var subscription: EventBus.SubscriptionHandle? = null

    suspend fun handleCollectedCardData(
        collectedData: NolPayPaymentCollectableData?
    ): Result<NolPayPaymentStep> = runSuspendCatching {
        return when (
            val collectedDataUnwrapped =
                requireNotNullCheck(collectedData, NolPayIllegalValueKey.COLLECTED_DATA)
        ) {
            is NolPayPaymentCollectableData.NolPayCardAndPhoneData ->
                tokenize(collectedDataUnwrapped)

            is NolPayPaymentCollectableData.NolPayTagData ->
                requiredActionInteractor(None()).flatMap { requiredAction ->
                    requestPayment(
                        collectedDataUnwrapped,
                        requiredAction
                    )
                }
        }
    }

    private suspend fun tokenize(
        collectedData: NolPayPaymentCollectableData.NolPayCardAndPhoneData
    ) = runSuspendCatching {
        asyncPaymentMethodConfigInteractor(
            AsyncPaymentMethodParams(PaymentMethodType.NOL_PAY.name)
        ).flatMapLatest { config ->
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    NolPayPaymentInstrumentParams(
                        PaymentMethodType.NOL_PAY.name,
                        config.paymentMethodConfigId,
                        config.locale,
                        collectedData.phoneCountryDiallingCode,
                        collectedData.mobileNumber,
                        collectedData.nolPaymentCard.cardNumber
                    ),
                    PrimerSessionIntent.CHECKOUT
                )
            )
        }.collect()
    }.mapSuspendCatching {
        startListeningForPendingEvents()
    }

    private suspend fun requestPayment(
        collectedData: NolPayPaymentCollectableData.NolPayTagData,
        requiredAction: NolPayRequiredAction
    ) = requestPaymentInteractor(
        NolPayRequestPaymentParams(
            collectedData.tag,
            requiredAction.transactionNumber
        )
    ).flatMap { completePaymentInteractor(NolPayCompletePaymentParams(requiredAction.completeUrl)) }
        .mapSuspendCatching { NolPayPaymentStep.PaymentRequested }

    private suspend fun startListeningForPendingEvents() =
        suspendCancellableCoroutine<NolPayPaymentStep> { cancellableContinuation ->
            subscription = EventBus.subscribe { checkoutEvent ->
                if (cancellableContinuation.isActive) {
                    when (checkoutEvent) {
                        is CheckoutEvent.StartNolPayFlow -> {
                            cancellableContinuation.resume(NolPayPaymentStep.CollectTagData)
                        }

                        else -> Unit
                    }
                } else {
                    subscription?.unregister()
                    subscription = null
                }
            }
            cancellableContinuation.invokeOnCancellation {
                subscription?.unregister()
                subscription = null
            }
        }
}
