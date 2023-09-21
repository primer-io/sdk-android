package io.primer.android.components.manager.nolPay.unlinkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.unlinkCard.di.NolPayUnlinkCardComponentProvider
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class NolPayUnlinkCardComponent internal constructor(
    private val unlinkPaymentCardDelegate: NolPayUnlinkPaymentCardDelegate,
    private val validatorRegistry: NolPayUnlinkDataValidatorRegistry,
    private val errorMapper: ErrorMapper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayUnlinkCollectableData>,
    PrimerHeadlessStepable<NolPayUnlinkCardStep>,
    PrimerHeadlessStartable {

    private val _stepFlow: MutableSharedFlow<NolPayUnlinkCardStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayUnlinkCardStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayUnlinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun updateCollectedData(collectedData: NolPayUnlinkCollectableData) {
        logSdkFunctionCalls(
            NolPayAnalyticsConstants.UNLINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
            mapOf(NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectedData.toString())
        )
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            _validationFlow.emit(
                validatorRegistry.getValidator(collectedData).validate(collectedData)
            )
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
                savedStateHandle
            ).onSuccess {
                _stepFlow.emit(it)
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_START_METHOD)
        viewModelScope.launch {
            unlinkPaymentCardDelegate.start().onSuccess {
                viewModelScope.launch {
                    _stepFlow.emit(NolPayUnlinkCardStep.CollectCardData)
                }
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    private fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = viewModelScope.launch {
        unlinkPaymentCardDelegate.logSdkAnalyticsEvent(methodName, context)
    }

    private fun handleError(
        throwable: Throwable,
    ) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
            .also { error ->
                _errorFlow.emit(error)
            }.also { error ->
                unlinkPaymentCardDelegate.logSdkAnalyticsErrors(error)
            }
    }

    companion object : DIAppComponent {
        fun provideInstance(owner: ViewModelStoreOwner) =
            NolPayUnlinkCardComponentProvider().provideInstance(owner)
    }
}
