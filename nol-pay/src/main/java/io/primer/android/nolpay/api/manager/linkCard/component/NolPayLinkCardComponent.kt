package io.primer.android.nolpay.api.manager.linkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.nolpay.api.manager.BaseNolPayComponent
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCardStep
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.nolpay.api.manager.linkCard.di.NolPayLinkCardComponentProvider
import io.primer.android.nolpay.implementation.linkCard.domain.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.nolpay.implementation.linkCard.presentation.NolPayLinkPaymentCardDelegate
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class NolPayLinkCardComponent internal constructor(
    private val linkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    validatorRegistry: NolPayLinkDataValidatorRegistry,
    eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    errorMapperRegistry: ErrorMapperRegistry,
    private val savedStateHandle: SavedStateHandle
) : BaseNolPayComponent<NolPayLinkCollectableData, NolPayLinkCardStep>(
    validatorRegistry = validatorRegistry,
    eventLoggingDelegate = eventLoggingDelegate,
    errorLoggingDelegate = errorLoggingDelegate,
    validationErrorLoggingDelegate = validationErrorLoggingDelegate,
    errorMapperRegistry = errorMapperRegistry
) {
    private val _collectedData: MutableSharedFlow<NolPayLinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_START_METHOD)
        viewModelScope.launch {
            linkPaymentCardDelegate.start()
                .onSuccess {
                    _componentStep.emit(NolPayLinkCardStep.CollectTagData)
                }.onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    override fun updateCollectedData(collectedData: NolPayLinkCollectableData) {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_UPDATE_COLLECTED_DATA_METHOD)
        viewModelScope.launch { _collectedData.emit(collectedData) }
        onCollectableDataUpdated(collectedData)
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            linkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.lastOrNull(),
                savedStateHandle
            ).onSuccess { step ->
                _componentStep.emit(step)
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    internal companion object {
        fun provideInstance(owner: ViewModelStoreOwner) =
            NolPayLinkCardComponentProvider().provideInstance(owner)
    }
}
