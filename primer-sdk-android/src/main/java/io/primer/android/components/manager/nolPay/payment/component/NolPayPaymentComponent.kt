package io.primer.android.components.manager.nolPay.payment.component

import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.components.manager.nolPay.BaseNolPayComponent
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.components.manager.nolPay.payment.di.NolPayStartPaymentComponentProvider
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.domain.error.ErrorMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class NolPayPaymentComponent internal constructor(
    private val startPaymentDelegate: NolPayStartPaymentDelegate,
    @Suppress("UnusedPrivateMember")
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate,
    eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    validatorRegistry: NolPayPaymentDataValidatorRegistry,
    errorMapper: ErrorMapper
) : BaseNolPayComponent<NolPayPaymentCollectableData, NolPayPaymentStep>(
    validatorRegistry = validatorRegistry,
    eventLoggingDelegate = eventLoggingDelegate,
    errorLoggingDelegate = errorLoggingDelegate,
    validationErrorLoggingDelegate = validationErrorLoggingDelegate,
    errorMapper = errorMapper
) {
    private val _collectedData: MutableSharedFlow<NolPayPaymentCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_START_METHOD)
        viewModelScope.launch {
            startPaymentDelegate.start().onSuccess {
                _componentStep.emit(NolPayPaymentStep.CollectCardAndPhoneData)
            }.onFailure { throwable ->
                handleError(throwable)
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
            startPaymentDelegate.handleCollectedCardData(
                _collectedData.replayCache.lastOrNull()
            ).onSuccess { step -> _componentStep.emit(step) }
                .onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    internal companion object {
        fun getInstance(owner: ViewModelStoreOwner) =
            NolPayStartPaymentComponentProvider().provideInstance(owner)
    }
}
