package io.primer.android.components.manager.nolPay.linkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.linkCard.di.NolPayLinkCardComponentProvider
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.error.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@ExperimentalPrimerApi
class NolPayLinkCardComponent internal constructor(
    private val linkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    private val dataValidatorRegistry: NolPayLinkDataValidatorRegistry,
    private val errorMapper: ErrorMapper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessStartable,
    PrimerHeadlessCollectDataComponent<NolPayLinkCollectableData>,
    PrimerHeadlessStepable<NolPayLinkCardStep> {

    private val _stepFlow: MutableSharedFlow<NolPayLinkCardStep> = MutableSharedFlow()
    override val step: Flow<NolPayLinkCardStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val error: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationErrors: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayLinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_START_METHOD)
        viewModelScope.launch {
            linkPaymentCardDelegate.start().onSuccess {
                viewModelScope.launch {
                    _stepFlow.emit(NolPayLinkCardStep.CollectTagData)
                }
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    override fun updateCollectedData(collectedData: NolPayLinkCollectableData) {
        logSdkFunctionCalls(
            NolPayAnalyticsConstants.LINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
            mapOf(NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectedData.toString())
        )
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            _validationFlow.emit(
                dataValidatorRegistry.getValidator(collectedData).validate(collectedData)
            )
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            linkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.lastOrNull(),
                savedStateHandle
            ).onSuccess { step ->
                _stepFlow.emit(step)
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    private fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = viewModelScope.launch {
        linkPaymentCardDelegate.logSdkAnalyticsEvent(methodName, context)
    }

    private fun handleError(
        throwable: Throwable,
    ) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
            .also { error ->
                _errorFlow.emit(error)
            }.also { error ->
                linkPaymentCardDelegate.logSdkAnalyticsErrors(error)
            }
    }

    internal companion object : DIAppComponent {
        fun provideInstance(owner: ViewModelStoreOwner) =
            NolPayLinkCardComponentProvider().provideInstance(owner)
    }
}
