package io.primer.android.components.manager.nolPay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.BuildConfig
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.core.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.PrimerNolPaymentCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

sealed interface NolPayUnlinkCollectableData : NolPayCollectableData {

    data class NolPayCardData(val nolPaymentCard: PrimerNolPaymentCard) :
        NolPayUnlinkCollectableData

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayUnlinkCollectableData

    data class NolPayOtpData(val otpCode: String) : NolPayUnlinkCollectableData
}

class NolPayUnlinkCardComponent internal constructor(
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val unlinkPaymentCardDelegate: NolPayUnlinkPaymentCardDelegate,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayUnlinkCollectableData>,
    PrimerHeadlessStepable<NolPayUnlinkDataStep>,
    PrimerHeadlessStartable {

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "32893fc5f6be4a5b95cbd7bbcceffd56" }
            }
        }
    }

    private val _stepFlow: MutableSharedFlow<NolPayUnlinkDataStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayUnlinkDataStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayUnlinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun updateCollectedData(t: NolPayUnlinkCollectableData) {
        viewModelScope.launch { _collectedData.emit(t) }
        viewModelScope.launch {
         //   _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        viewModelScope.launch {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
                savedStateHandle
            ).onSuccess {
                _stepFlow.emit(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun start() {
        viewModelScope.launch {
            nolPayConfigurationInteractor(None()).collectLatest { configuration ->
                initSDK(configuration)
            }
        }
    }

    private suspend fun initSDK(configuration: NolPayConfiguration) = runSuspendCatching {
        PrimerNolPay.instance.initSDK(
            configuration.environment != Environment.PRODUCTION,
            BuildConfig.DEBUG,
            configuration.merchantAppId,
            handler
        )
    }.onSuccess {
        viewModelScope.launch {
            _stepFlow.emit(NolPayUnlinkDataStep.COLLECT_CARD_DATA)
        }
    }.onFailure {
        it.printStackTrace()
    }

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return NolPayUnlinkCardComponent(
                            get(),
                            get(),
                            get(),
                            get(),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[NolPayUnlinkCardComponent::class.java]
        }
    }
}
