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
import io.primer.android.components.data.payments.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.core.composable.Collectable
import io.primer.android.components.manager.core.composable.Errorable
import io.primer.android.components.manager.core.composable.PrimerHeadlessManager
import io.primer.android.components.manager.core.composable.Submitable
import io.primer.android.components.manager.core.composable.Validatable
import io.primer.android.components.manager.nolPay.composable.Stepable
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.PrimerNolPaymentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

sealed interface NolPayIntent {
    object LinkPaymentCard : NolPayIntent
    data class UnlinkPaymentCard(val nolPaymentCard: PrimerNolPaymentCard) : NolPayIntent

    data class StartPaymentFlow(val nolPaymentCard: PrimerNolPaymentCard) : NolPayIntent
}

enum class NolPayCollectDataStep {

    COLLECT_PHONE_DATA,
    COLLECT_OTP_DATA,
    COLLECT_TAG_DATA
}

sealed interface NolPayResult {

    data class PaymentCardLinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayResult
    data class PaymentCardUnlinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayResult
    data class PaymentFlowStarted(val nolPaymentCard: PrimerNolPaymentCard) : NolPayResult
}

sealed interface NolPayData {

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayData

    data class NolPayOtpData(val otpCode: String) : NolPayData
    data class NolPayTagData(val tag: Tag) : NolPayData
}

class PrimerHeadlessUniveralCheckoutNolPayManager internal constructor(
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val nolPayLinkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessManager,
    Collectable<NolPayData>,
    Stepable<NolPayCollectDataStep>,
    Errorable,
    Validatable,
    Submitable {

    private val _stepFlow: MutableSharedFlow<NolPayCollectDataStep> = MutableSharedFlow()
    override val collectDataStepFlow: SharedFlow<NolPayCollectDataStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayData> =
        MutableSharedFlow(replay = 1)
    override val collectedData: SharedFlow<NolPayData> = _collectedData

    val resultFlow: SharedFlow<NolPayResult> = MutableSharedFlow<NolPayResult>()

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "32893fc5f6be4a5b95cbd7bbcceffd56" }
            }
        }
    }

    fun start(nolPayIntent: NolPayIntent) {
        viewModelScope.launch {
            nolPayConfigurationInteractor(None()).collectLatest { configuration ->
                initSDK(configuration)
            }
        }
    }

    override fun updateCollectedData(t: NolPayData) {
        viewModelScope.launch { _collectedData.emit(t) }
        viewModelScope.launch {
            _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        viewModelScope.launch {
            nolPayLinkPaymentCardDelegate.handleCollectedCardData(
                collectedData.replayCache.last(),
                _stepFlow,
                _errorFlow,
                savedStateHandle
            )
        }
    }

    fun createPayment(t: NolPayData.NolPayTagData) = viewModelScope.launch(Dispatchers.IO) {
        PrimerNolPay.instance.createPayment(t.tag, "d")
    }

    private fun initSDK(configuration: NolPayConfiguration) {
        PrimerNolPay.instance.initSDK(
            configuration.environment != Environment.PRODUCTION,
            BuildConfig.DEBUG, configuration.merchantAppId, handler
        )

        viewModelScope.launch {
            _stepFlow.emit(NolPayCollectDataStep.COLLECT_TAG_DATA)
        }
    }

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): PrimerHeadlessUniveralCheckoutNolPayManager {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return PrimerHeadlessUniveralCheckoutNolPayManager(
                            get(),
                            get(),
                            get(),
                            get(),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[PrimerHeadlessUniveralCheckoutNolPayManager::class.java]
        }
    }
}
