package io.primer.android.components.manager.nolPay

import android.nfc.Tag
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.BuildConfig
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

interface NolPayCollectableData : PrimerCollectableData
sealed interface NolPayLinkCollectableData : NolPayCollectableData {

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayLinkCollectableData

    data class NolPayOtpData(val otpCode: String) : NolPayLinkCollectableData
    data class NolPayTagData(val tag: Tag) : NolPayLinkCollectableData
}

class NolPayLinkCardComponent internal constructor(
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val nolPayLinkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayLinkCollectableData>,
    PrimerHeadlessStepable<NolPayLinkDataStep>,
    PrimerHeadlessStartable {

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "2675dcb9cc034bddbd1ad48908840542" }
            }
        }
    }

    private val _stepFlow: MutableSharedFlow<NolPayLinkDataStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayLinkDataStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayLinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun updateCollectedData(t: NolPayLinkCollectableData) {
        viewModelScope.launch { _collectedData.emit(t) }
        viewModelScope.launch {
            _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        viewModelScope.launch {
            nolPayLinkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
                savedStateHandle
            ).onSuccess { step ->
                _stepFlow.emit(step)
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
            _stepFlow.emit(NolPayLinkDataStep.COLLECT_TAG_DATA)
        }
    }.onFailure {
        it.printStackTrace()
    }

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): NolPayLinkCardComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return NolPayLinkCardComponent(
                            get(),
                            get(),
                            get(),
                            get(),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[NolPayLinkCardComponent::class.java]
        }
    }
}
