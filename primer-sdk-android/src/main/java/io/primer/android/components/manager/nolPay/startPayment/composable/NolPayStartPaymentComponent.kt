package io.primer.android.components.manager.nolPay.startPayment.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentCollectableData
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentStep
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.di.DIAppComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.get

@ExperimentalPrimerApi
class NolPayStartPaymentComponent internal constructor(
    private val startPaymentDelegate: NolPayStartPaymentDelegate,
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayStartPaymentCollectableData>,
    PrimerHeadlessStepable<NolPayStartPaymentStep>,
    PrimerHeadlessStartable {

    private val _stepFlow: MutableSharedFlow<NolPayStartPaymentStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayStartPaymentStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val error: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationErrors: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayStartPaymentCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun start() {
        viewModelScope.launch {
            startPaymentDelegate.start().onSuccess {
                _stepFlow.emit(NolPayStartPaymentStep.CollectStartPaymentData)
            }.onFailure {
            }
        }
    }

    override fun updateCollectedData(collectedData: NolPayStartPaymentCollectableData) {
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            //   _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        viewModelScope.launch {
            startPaymentDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
            ).onSuccess {
                _stepFlow.emit(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    internal companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): NolPayStartPaymentComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return NolPayStartPaymentComponent(
                            get(),
                        ) as T
                    }
                }
            )[NolPayStartPaymentComponent::class.java]
        }
    }
}
