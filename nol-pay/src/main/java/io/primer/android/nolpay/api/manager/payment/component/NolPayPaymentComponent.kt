package io.primer.android.nolpay.api.manager.payment.component

import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.nolpay.api.manager.BaseNolPayComponent
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentStep
import io.primer.android.nolpay.api.manager.payment.di.NolPayStartPaymentComponentProvider
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.nolpay.implementation.errors.data.exception.NolPayIllegalValueKey
import io.primer.android.nolpay.implementation.paymentCard.payment.delegate.NolPayPaymentDelegate
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.NolPayTokenizationDelegate
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.composable.NolPayTokenizationInputable
import io.primer.android.nolpay.implementation.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class NolPayPaymentComponent internal constructor(
    private val baseNolPayDelegate: BaseNolPayDelegate,
    private val tokenizationDelegate: NolPayTokenizationDelegate,
    private val paymentDelegate: NolPayPaymentDelegate,
    eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    validatorRegistry: NolPayPaymentDataValidatorRegistry,
    errorMapperRegistry: ErrorMapperRegistry
) : BaseNolPayComponent<NolPayPaymentCollectableData, NolPayPaymentStep>(
    errorLoggingDelegate = errorLoggingDelegate,
    validatorRegistry = validatorRegistry,
    eventLoggingDelegate = eventLoggingDelegate,
    validationErrorLoggingDelegate = validationErrorLoggingDelegate,
    errorMapperRegistry = errorMapperRegistry
) {
    private val _collectedData: MutableSharedFlow<NolPayPaymentCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_START_METHOD)
        viewModelScope.launch {
            launch {
                baseNolPayDelegate.start().onSuccess {
                    _componentStep.emit(NolPayPaymentStep.CollectCardAndPhoneData)
                }.onFailure { throwable ->
                    handleError(throwable)
                }
            }

            launch {
                paymentDelegate.componentStep.collect { step ->
                    _componentStep.emit(step)
                }
            }
        }
    }

    override fun updateCollectedData(collectedData: NolPayPaymentCollectableData) {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_UPDATE_COLLECTED_DATA_METHOD)
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            onCollectableDataUpdated(collectedData)
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            handleCollectedCardData(
                _collectedData.replayCache.lastOrNull()
            ).onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    private suspend fun handleCollectedCardData(
        collectedData: NolPayPaymentCollectableData?
    ): Result<Unit> = runSuspendCatching {
        return when (
            val collectedDataUnwrapped =
                requireNotNullCheck(collectedData, NolPayIllegalValueKey.COLLECTED_DATA)
        ) {
            // if there's a tokenization/payment error, we don't want to propagate that to the component's error
            is NolPayPaymentCollectableData.NolPayCardAndPhoneData ->
                tokenizationDelegate.tokenize(
                    NolPayTokenizationInputable(
                        mobileNumber = collectedDataUnwrapped.mobileNumber,
                        nolPayCardNumber = collectedDataUnwrapped.nolPaymentCard.cardNumber,
                        paymentMethodType = PaymentMethodType.NOL_PAY.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT
                    )
                ).flatMap { paymentMethodTokenData ->
                    paymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, PrimerSessionIntent.CHECKOUT)
                }.onFailure { throwable ->
                    paymentDelegate.handleError(throwable)
                }.map { }.recover { }

            is NolPayPaymentCollectableData.NolPayTagData ->
                paymentDelegate.requestPayment(
                    collectedData = collectedDataUnwrapped
                ).onFailure { throwable -> handleError(throwable = throwable) }
                    .flatMap {
                        paymentDelegate.completePayment()
                    }
        }
    }

    internal companion object {
        fun getInstance(owner: ViewModelStoreOwner) =
            NolPayStartPaymentComponentProvider().provideInstance(owner)
    }
}
