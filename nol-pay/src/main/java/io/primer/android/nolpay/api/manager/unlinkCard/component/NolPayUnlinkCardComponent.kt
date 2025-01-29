package io.primer.android.nolpay.api.manager.unlinkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.nolpay.api.manager.BaseNolPayComponent
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.nolpay.api.manager.unlinkCard.di.NolPayUnlinkCardComponentProvider
import io.primer.android.nolpay.implementation.unlinkCard.presentation.NolPayUnlinkPaymentCardDelegate
import io.primer.android.nolpay.implementation.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class NolPayUnlinkCardComponent internal constructor(
    private val unlinkPaymentCardDelegate: NolPayUnlinkPaymentCardDelegate,
    eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    validatorRegistry: NolPayUnlinkDataValidatorRegistry,
    errorMapperRegistry: ErrorMapperRegistry,
    private val savedStateHandle: SavedStateHandle,
) : BaseNolPayComponent<NolPayUnlinkCollectableData, NolPayUnlinkCardStep>(
    validatorRegistry = validatorRegistry,
    eventLoggingDelegate = eventLoggingDelegate,
    errorLoggingDelegate = errorLoggingDelegate,
    validationErrorLoggingDelegate = validationErrorLoggingDelegate,
    errorMapperRegistry = errorMapperRegistry,
) {
    private val collectedData: MutableSharedFlow<NolPayUnlinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_START_METHOD)
        viewModelScope.launch {
            unlinkPaymentCardDelegate.start()
                .onSuccess {
                    viewModelScope.launch {
                        _componentStep.emit(NolPayUnlinkCardStep.CollectCardAndPhoneData)
                    }
                }.onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    override fun updateCollectedData(collectedData: NolPayUnlinkCollectableData) {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_UPDATE_COLLECTED_DATA_METHOD)
        viewModelScope.launch { this@NolPayUnlinkCardComponent.collectedData.emit(collectedData) }
        viewModelScope.launch {
            onCollectableDataUpdated(collectedData)
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                collectedData.replayCache.lastOrNull(),
                savedStateHandle,
            ).onSuccess {
                _componentStep.emit(it)
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    internal companion object {
        fun provideInstance(owner: ViewModelStoreOwner) = NolPayUnlinkCardComponentProvider().provideInstance(owner)
    }
}
