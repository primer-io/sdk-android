package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.AsyncPaymentMethodConfigInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayTransactionNumberInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequestPaymentParams
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentCollectableData
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentStep
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.None
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class NolPayStartPaymentDelegate(
    private val asyncPaymentMethodConfigInteractor: AsyncPaymentMethodConfigInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val requestPaymentInteractor: NolPayRequestPaymentInteractor,
    private val transactionNumberInteractor: NolPayTransactionNumberInteractor,
    appSecretInteractor: NolPayAppSecretInteractor,
    configurationInteractor: NolPayConfigurationInteractor,
    analyticsInteractor: AnalyticsInteractor
) : BaseNolPayDelegate(appSecretInteractor, configurationInteractor, analyticsInteractor) {

    suspend fun startListeningForEvents() =
        suspendCancellableCoroutine<NolPayStartPaymentStep> { cancellableContinuation ->
            var subscription: EventBus.SubscriptionHandle? = EventBus.subscribe { checkoutEvent ->
                when (checkoutEvent) {
                    is CheckoutEvent.StartNolPayFlow -> {
                        cancellableContinuation.resume(NolPayStartPaymentStep.CollectTagData)
                    }

                    else -> Unit
                }
            }
            cancellableContinuation.invokeOnCancellation {
                subscription?.unregister()
                subscription = null
            }
        }

    suspend fun handleCollectedCardData(
        collectedData: NolPayStartPaymentCollectableData?,
    ): Result<*> = runSuspendCatching {
        return when (
            val collectedDataUnwrapped =
                requireNotNullCheck(collectedData, NolPayIllegalValueKey.COLLECTED_DATA)
        ) {
            is NolPayStartPaymentCollectableData.NolPayStartPaymentData ->
                tokenize(collectedDataUnwrapped)

            is NolPayStartPaymentCollectableData.NolPayTagData ->
                transactionNumberInteractor(None()).onSuccess { transactionNumber ->
                    requestPayment(
                        collectedDataUnwrapped,
                        transactionNumber
                    )
                }
        }
    }

    private suspend fun tokenize(
        collectedData: NolPayStartPaymentCollectableData.NolPayStartPaymentData
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
        }.first()
    }

    private suspend fun requestPayment(
        collectedData: NolPayStartPaymentCollectableData.NolPayTagData,
        transactionNo: String
    ) = requestPaymentInteractor(NolPayRequestPaymentParams(collectedData.tag, transactionNo))
}
