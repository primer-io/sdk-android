package io.primer.android.components.manager.nolPay.startPayment.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentCollectableData
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentStep
import io.primer.android.components.manager.nolPay.startPayment.di.NolPayStartPaymentComponentProvider
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.error.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@ExperimentalPrimerApi
class NolPayStartPaymentComponent internal constructor(
    private val startPaymentDelegate: NolPayStartPaymentDelegate,
    private val dataValidatorRegistry: NolPayPaymentDataValidatorRegistry,
    private val errorMapper: ErrorMapper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayStartPaymentCollectableData>,
    PrimerHeadlessStepable<NolPayStartPaymentStep>,
    PrimerHeadlessStartable {

    private val _componentStep: MutableSharedFlow<NolPayStartPaymentStep> = MutableSharedFlow()
    override val componentStep: Flow<NolPayStartPaymentStep> = _componentStep

    private val _componentError: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val componentError: SharedFlow<PrimerError> = _componentError

    private val _componentValidationErrors: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val componentValidationErrors: SharedFlow<List<PrimerValidationError>> =
        _componentValidationErrors

    private val _collectedData: MutableSharedFlow<NolPayStartPaymentCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_START_METHOD)
        viewModelScope.launch {
            _componentStep.emit(startPaymentDelegate.startListeningForEvents())
        }
        viewModelScope.launch {
            startPaymentDelegate.start().onSuccess {
                _componentStep.emit(NolPayStartPaymentStep.CollectStartPaymentData)
            }.onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    override fun updateCollectedData(collectedData: NolPayStartPaymentCollectableData) {
        logSdkFunctionCalls(
            NolPayAnalyticsConstants.PAYMENT_UPDATE_COLLECTED_DATA_METHOD,
            mapOf(NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectedData.toString())
        )
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            _componentValidationErrors.emit(
                dataValidatorRegistry.getValidator(collectedData).validate(collectedData)
            )
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.PAYMENT_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            startPaymentDelegate.handleCollectedCardData(
                _collectedData.replayCache.lastOrNull(),
            ).onFailure { throwable ->
                handleError(throwable)
            }
        }
    }

    private fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = viewModelScope.launch {
        startPaymentDelegate.logSdkAnalyticsEvent(methodName, context)
    }

    private fun handleError(
        throwable: Throwable,
    ) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
            .also { error ->
                _componentError.emit(error)
            }.also { error ->
                startPaymentDelegate.logSdkAnalyticsErrors(error)
            }
    }

    internal companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner) =
            NolPayStartPaymentComponentProvider().provideInstance(owner)
    }
}
