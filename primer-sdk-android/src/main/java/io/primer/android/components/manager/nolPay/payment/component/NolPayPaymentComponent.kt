package io.primer.android.components.manager.nolPay.payment.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.components.manager.nolPay.payment.di.NolPayStartPaymentComponentProvider
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.core.extensions.debounce
import io.primer.android.domain.error.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class NolPayPaymentComponent internal constructor(
    private val startPaymentDelegate: NolPayStartPaymentDelegate,
    @Suppress("UnusedPrivateMember")
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate,
    private val validatorRegistry: NolPayPaymentDataValidatorRegistry,
    private val errorMapper: ErrorMapper
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayPaymentCollectableData>,
    PrimerHeadlessStepable<NolPayPaymentStep>,
    PrimerHeadlessStartable {

    private val _componentStep: MutableSharedFlow<NolPayPaymentStep> = MutableSharedFlow()
    override val componentStep: Flow<NolPayPaymentStep> = _componentStep

    private val _componentError: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val componentError: Flow<PrimerError> = _componentError

    private val _componentValidationStatus:
        MutableSharedFlow<PrimerValidationStatus<NolPayPaymentCollectableData>> =
            MutableSharedFlow()
    override val componentValidationStatus:
        Flow<PrimerValidationStatus<NolPayPaymentCollectableData>> = _componentValidationStatus

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

    private fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = viewModelScope.launch {
        startPaymentDelegate.logSdkAnalyticsEvent(methodName, context)
    }

    private fun handleError(
        throwable: Throwable
    ) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
            .also { error ->
                _componentError.emit(error)
            }.also { error ->
                startPaymentDelegate.logSdkAnalyticsErrors(error)
            }
    }

    private val onCollectableDataUpdated: (NolPayPaymentCollectableData) -> Unit =
        viewModelScope.debounce { collectedData ->
            _componentValidationStatus.emit(PrimerValidationStatus.Validating(collectedData))
            val validationResult = validatorRegistry.getValidator(collectedData).validate(
                collectedData
            )
            validationResult.onSuccess { errors ->
                _componentValidationStatus.emit(
                    when (errors.isEmpty()) {
                        true -> PrimerValidationStatus.Valid(collectedData)
                        false -> PrimerValidationStatus.Invalid(errors, collectedData)
                    }
                )
            }.onFailure { throwable ->
                _componentValidationStatus.emit(
                    PrimerValidationStatus.Error(
                        errorMapper.getPrimerError(throwable),
                        collectedData
                    )
                )
            }
        }

    internal companion object {
        fun getInstance(owner: ViewModelStoreOwner) =
            NolPayStartPaymentComponentProvider().provideInstance(owner)
    }
}
